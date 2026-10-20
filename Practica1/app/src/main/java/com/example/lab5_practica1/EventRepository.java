package com.example.lab5_practica1;

import android.content.*;
import android.os.*;
import androidx.lifecycle.*;
import com.example.lab5_practica1.common.NotificationHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.*;
import java.util.concurrent.*;

public final class EventRepository {
  public interface Done {
    void result(String error);
  }

  private static volatile EventRepository instance;
  private final Context context;
  private final android.content.SharedPreferences prefs;
  private final ExecutorService worker = Executors.newSingleThreadExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  private final Gson gson = new Gson();
  public final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
  public final MutableLiveData<String> error = new MutableLiveData<>();

  private EventRepository(Context c) {
    context = c.getApplicationContext();
    prefs = context.getSharedPreferences("agenda_eventos", Context.MODE_PRIVATE);
    refresh();
  }

  public static EventRepository get(Context c) {
    if (instance == null)
      synchronized (EventRepository.class) {
        if (instance == null) instance = new EventRepository(c);
      }
    return instance;
  }

  private List<Event> read() {
    List<Event> list =
        gson.fromJson(
            prefs.getString("eventos", "[]"), new TypeToken<ArrayList<Event>>() {}.getType());
    if (list == null) throw new IllegalStateException("Lista de eventos dañada");
    HashSet<Integer> ids = new HashSet<>();
    for (Event e : list)
      if (e == null || e.id <= 0 || !ids.add(e.id) || e.name == null || e.date == null)
        throw new IllegalStateException("Hay un evento dañado");
    for (Event e : list) {
      java.time.LocalDate.parse(e.date);
      if (e.timeMinutes < 0 || e.timeMinutes > 1439) throw new IllegalStateException("Hora dañada");
    }
    return list;
  }

  public void refresh() {
    refresh(() -> {});
  }

  public void refresh(Runnable finished) {
    worker.execute(
        () -> {
          try {
            List<Event> list = read();
            events.postValue(list);
            for (Event e : list) AlarmScheduler.schedule(context, e);
          } catch (Exception ex) {
            error.postValue("No se pudo leer la agenda: " + ex.getMessage());
          } finally {
            finished.run();
          }
        });
  }

  public void save(Event e, Done done) {
    worker.execute(
        () -> {
          String problem = null;
          try {
            String invalid = EventDates.validate(e, java.time.LocalDateTime.now());
            if (invalid != null) throw new IllegalArgumentException(invalid);
            List<Event> list = read();
            int nextId = prefs.getInt("next_id", 1);
            if (e.id == 0) {
              e.id = nextId++;
            } else if (list.stream().noneMatch(x -> x.id == e.id))
              throw new IllegalStateException("El evento ya no existe");
            e.name = e.name.trim();
            e.revision = System.currentTimeMillis();
            list.removeIf(x -> x.id == e.id);
            list.add(e);
            if (!prefs
                .edit()
                .putString("eventos", gson.toJson(list))
                .putInt("next_id", nextId)
                .commit()) throw new IllegalStateException("No se pudo guardar");
            AlarmScheduler.cancel(context, e.id);
            NotificationHelper.cancel(context, "event-" + e.id);
            context
                .getSharedPreferences("avisos_emitidos", 0)
                .edit()
                .remove("event-" + e.id)
                .commit();
            AlarmScheduler.schedule(context, e);
            events.postValue(list);
          } catch (Exception ex) {
            problem = ex.getMessage();
          }
          String result = problem;
          main.post(() -> done.result(result));
        });
  }

  public void delete(Event e, Done done) {
    worker.execute(
        () -> {
          String problem = null;
          try {
            List<Event> list = read();
            list.removeIf(x -> x.id == e.id);
            if (!prefs.edit().putString("eventos", gson.toJson(list)).commit())
              throw new IllegalStateException("No se pudo eliminar");
            AlarmScheduler.cancel(context, e.id);
            NotificationHelper.cancel(context, "event-" + e.id);
            NotificationHelper.cancel(context, "test-event-" + e.id);
            context
                .getSharedPreferences("avisos_emitidos", 0)
                .edit()
                .remove("event-" + e.id)
                .commit();
            events.postValue(list);
          } catch (Exception ex) {
            problem = ex.getMessage();
          }
          String result = problem;
          main.post(() -> done.result(result));
        });
  }

  public void deliver(int id, long revision, long occurrence, Runnable finished) {
    worker.execute(
        () -> {
          try {
            for (Event e : read())
              if (e.id == id && e.revision == revision) {
                android.content.SharedPreferences sent =
                    context.getSharedPreferences("avisos_emitidos", 0);
                if (sent.getLong("event-" + id, Long.MIN_VALUE) != occurrence) {
                  java.time.LocalDateTime at =
                      java.time.LocalDateTime.ofInstant(
                          java.time.Instant.ofEpochMilli(occurrence),
                          java.time.ZoneId.systemDefault());
                  boolean shown =
                      NotificationHelper.show(
                          context,
                          "event-" + id,
                          e.annual,
                          e.name,
                          "Evento: " + EventDates.label(e, at),
                          new Intent(context, EventFormActivity.class).putExtra("event_id", id));
                  // Se registra el intento, incluso si no hay permiso, para no generar un bucle de
                  // alarmas.
                  sent.edit().putLong("event-" + id, occurrence).commit();
                }
                AlarmScheduler.schedule(context, e);
                break;
              }
          } catch (Exception ex) {
            error.postValue("No se pudo procesar el aviso: " + ex.getMessage());
          } finally {
            finished.run();
          }
        });
  }
}
