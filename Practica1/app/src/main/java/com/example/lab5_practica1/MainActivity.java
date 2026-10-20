package com.example.lab5_practica1;

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica1.common.*;
import com.example.lab5_practica1.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.*;
import java.util.*;

public class MainActivity extends BaseActivity implements EventAdapter.Actions {
  private ActivityMainBinding b;
  private AgendaViewModel model;
  private EventAdapter adapter;
  private final Handler clock = new Handler(Looper.getMainLooper());
  private final Runnable tick =
      new Runnable() {
        @Override
        public void run() {
          render();
          clock.postDelayed(this, 60000);
        }
      };

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityMainBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    model = new ViewModelProvider(this).get(AgendaViewModel.class);
    adapter = new EventAdapter(this);
    b.recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    b.recycler.setAdapter(adapter);
    b.addButton.setOnClickListener(v -> startActivity(new Intent(this, EventFormActivity.class)));
    b.calendarButton.setOnClickListener(
        v -> startActivity(new Intent(this, CalendarActivity.class)));
    b.permissionButton.setOnClickListener(v -> askNotifications());
    b.precisionButton.setOnClickListener(
        v -> {
          if (Build.VERSION.SDK_INT >= 31)
            startActivity(
                new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName())));
        });
    model.repository.events.observe(this, list -> render());
    model.repository.error.observe(this, this::message);
    model.result.observe(
        this,
        r -> {
          if (r != null && r.consume()) message(r.message);
        });
  }

  private void render() {
    List<Event> list = model.repository.events.getValue();
    if (list == null) list = Collections.emptyList();
    adapter.submit(list, LocalDateTime.now());
    b.emptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    b.statusText.setText(
        "Avisos: "
            + (NotificationHelper.allowed(this) ? "permitidos" : "sin permiso")
            + " · Horario: "
            + (AlarmScheduler.exact(this) ? "exacto" : "aproximado (Android puede retrasarlo)"));
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (model != null) {
      model.repository.refresh();
      clock.post(tick);
    }
  }

  @Override
  protected void onPause() {
    clock.removeCallbacks(tick);
    super.onPause();
  }

  @Override
  protected void onNotificationPermissionChanged() {
    render();
  }

  @Override
  public void edit(Event e) {
    startActivity(new Intent(this, EventFormActivity.class).putExtra("event_id", e.id));
  }

  @Override
  public void delete(Event e) {
    new MaterialAlertDialogBuilder(this)
        .setTitle("¿Eliminar " + e.name + "?")
        .setMessage("También se cancelará su recordatorio.")
        .setNegativeButton("Volver", null)
        .setPositiveButton("Eliminar", (d, w) -> model.delete(e))
        .show();
  }

  @Override
  public void test(Event e) {
    boolean sent =
        NotificationHelper.show(
            this,
            "test-event-" + e.id,
            e.annual,
            "Prueba: " + e.name,
            "Evento: " + EventDates.label(e, EventDates.next(e, LocalDateTime.now())),
            new Intent(this, EventFormActivity.class).putExtra("event_id", e.id));
    message(
        sent
            ? "Prueba enviada. No cambia el recordatorio programado."
            : "Permite las notificaciones y revisa el canal en Ajustes.");
  }
}
