package com.example.lab5_practica2;

import static org.junit.Assert.*;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.service.notification.StatusBarNotification;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.lab5_practica2.common.NotificationHelper;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationFlowTest {
  @Test
  public void distinctNotificationsKeepTheirOwnDestinationAndCanBeCancelled() throws Exception {
    Instrumentation instrument = InstrumentationRegistry.getInstrumentation();
    Context c = instrument.getTargetContext();
    instrument
        .getUiAutomation()
        .grantRuntimePermission(c.getPackageName(), Manifest.permission.POST_NOTIFICATIONS);
    NotificationHelper.createChannels(c);
    NotificationManager manager = c.getSystemService(NotificationManager.class);
    String key = "instrumentation-probe-A";
    try (androidx.test.core.app.ActivityScenario<MainActivity> foreground =
        androidx.test.core.app.ActivityScenario.launch(MainActivity.class)) {
      assertTrue(
          NotificationHelper.show(
              c,
              key,
              true,
              "Registro A",
              "Prueba de destino",
              new Intent(c, ItemFormActivity.class).putExtra("item_id", 391L)));
      assertTrue(
          NotificationHelper.show(
              c,
              "instrumentation-probe-B",
              false,
              "Registro B",
              "Otro destino",
              new Intent(c, MainActivity.class)));
      StatusBarNotification found = null;
      for (int attempt = 0; attempt < 30 && found == null; attempt++) {
        for (StatusBarNotification s : manager.getActiveNotifications())
          if (key.equals(s.getTag())) found = s;
        if (found == null) Thread.sleep(100);
      }
      assertNotNull(found);
      assertEquals(NotificationHelper.HIGH, found.getNotification().getChannelId());
      Instrumentation.ActivityMonitor monitor =
          instrument.addMonitor(ItemFormActivity.class.getName(), null, false);
      try {
        found.getNotification().contentIntent.send();
        Activity opened = monitor.waitForActivityWithTimeout(8000);
        assertNotNull(opened);
        assertEquals(391L, opened.getIntent().getLongExtra("item_id", -1));
        instrument.runOnMainSync(opened::finish);
      } finally {
        instrument.removeMonitor(monitor);
      }
      NotificationHelper.cancel(c, key);
      for (StatusBarNotification s : manager.getActiveNotifications())
        assertNotEquals(key, s.getTag());
    } finally {
      NotificationHelper.cancel(c, key);
      NotificationHelper.cancel(c, "instrumentation-probe-B");
    }
  }
}
