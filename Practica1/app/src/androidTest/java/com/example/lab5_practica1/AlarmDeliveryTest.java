package com.example.lab5_practica1;

import static org.junit.Assert.*;

import android.Manifest;
import android.app.*;
import android.content.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.lab5_practica1.common.NotificationHelper;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AlarmDeliveryTest {
  @Test
  public void receiverLogicChecksRevisionAndDeliversOnlyOnce() throws Exception {
    Instrumentation instrument = InstrumentationRegistry.getInstrumentation();
    Context c = instrument.getTargetContext();
    instrument
        .getUiAutomation()
        .grantRuntimePermission(c.getPackageName(), Manifest.permission.POST_NOTIFICATIONS);
    EventRepository repository = EventRepository.get(c);
    Event e = new Event();
    e.name = "Prueba de entrega";
    e.date = LocalDate.now().plusDays(20).toString();
    e.annual = true;
    CountDownLatch saved = new CountDownLatch(1);
    AtomicReference<String> error = new AtomicReference<>();
    repository.save(
        e,
        result -> {
          error.set(result);
          saved.countDown();
        });
    assertTrue(saved.await(10, TimeUnit.SECONDS));
    assertNull(error.get());
    String key = "event-" + e.id;
    long occurrence =
        EventDates.next(e, LocalDateTime.now())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    try {
      CountDownLatch stale = new CountDownLatch(1);
      repository.deliver(e.id, e.revision - 1, occurrence, stale::countDown);
      assertTrue(stale.await(10, TimeUnit.SECONDS));
      assertEquals(
          Long.MIN_VALUE,
          c.getSharedPreferences("avisos_emitidos", 0).getLong(key, Long.MIN_VALUE));
      CountDownLatch delivered = new CountDownLatch(1);
      repository.deliver(e.id, e.revision, occurrence, delivered::countDown);
      assertTrue(delivered.await(10, TimeUnit.SECONDS));
      assertEquals(occurrence, c.getSharedPreferences("avisos_emitidos", 0).getLong(key, 0));
      boolean found = false;
      for (android.service.notification.StatusBarNotification s :
          c.getSystemService(NotificationManager.class).getActiveNotifications())
        if (key.equals(s.getTag())) {
          found = true;
          assertEquals(NotificationHelper.HIGH, s.getNotification().getChannelId());
          assertEquals(e.name, s.getNotification().extras.getString(Notification.EXTRA_TITLE));
        }
      assertTrue(found);
      NotificationHelper.cancel(c, key);
      CountDownLatch repeat = new CountDownLatch(1);
      repository.deliver(e.id, e.revision, occurrence, repeat::countDown);
      assertTrue(repeat.await(10, TimeUnit.SECONDS));
      for (android.service.notification.StatusBarNotification s :
          c.getSystemService(NotificationManager.class).getActiveNotifications())
        assertNotEquals(key, s.getTag());
    } finally {
      CountDownLatch deleted = new CountDownLatch(1);
      repository.delete(e, result -> deleted.countDown());
      assertTrue(deleted.await(10, TimeUnit.SECONDS));
    }
  }
}
