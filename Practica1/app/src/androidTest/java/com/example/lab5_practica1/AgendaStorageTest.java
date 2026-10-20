package com.example.lab5_practica1;

import static org.junit.Assert.*;

import android.app.*;
import android.content.*;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AgendaStorageTest {
  private Context c;
  private EventRepository repository;
  private Event event;

  private void await(java.util.function.Consumer<EventRepository.Done> operation) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> error = new AtomicReference<>();
    operation.accept(
        result -> {
          error.set(result);
          latch.countDown();
        });
    assertTrue(latch.await(10, TimeUnit.SECONDS));
    assertNull(error.get());
  }

  @Before
  public void setup() {
    c = InstrumentationRegistry.getInstrumentation().getTargetContext();
    repository = EventRepository.get(c);
  }

  @After
  public void cleanup() throws Exception {
    if (event != null && event.id != 0) await(done -> repository.delete(event, done));
  }

  @Test
  public void saveEditDeleteKeepsIdAndCancelsAlarm() throws Exception {
    event = new Event();
    event.name = "Prueba instrumentada";
    event.date = LocalDate.now().plusDays(30).toString();
    event.leadDays = 1;
    await(done -> repository.save(event, done));
    int id = event.id;
    assertTrue(id > 0);
    assertTrue(
        c.getSharedPreferences("agenda_eventos", 0)
            .getString("eventos", "")
            .contains("Prueba instrumentada"));
    event.name = "Prueba editada";
    event.leadDays = 7;
    await(done -> repository.save(event, done));
    assertEquals(id, event.id);
    Intent intent =
        new Intent(c, EventAlarmReceiver.class)
            .setAction("AGENDA_EVENT")
            .setData(Uri.parse("agenda://event/" + id));
    assertNotNull(
        PendingIntent.getBroadcast(
            c, 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE));
    await(done -> repository.delete(event, done));
    assertNull(
        PendingIntent.getBroadcast(
            c, 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE));
    assertFalse(
        c.getSharedPreferences("agenda_eventos", 0)
            .getString("eventos", "")
            .contains("Prueba editada"));
    event = null;
  }
}
