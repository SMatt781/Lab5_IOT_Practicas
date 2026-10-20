package com.example.lab5_practica3;

import static org.junit.Assert.*;

import android.app.*;
import android.os.*;
import android.view.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.lab5_practica3.common.NotificationHelper;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UiSmokeTest {
  @Test
  public void mainScreenHasVisibleControlsAndRecycler() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          a -> {
            assertNotNull(a.findViewById(R.id.toolbar));
            assertTrue(a.findViewById(R.id.addButton).isShown());
            assertNotNull(a.findViewById(R.id.recycler));
            assertTrue(a.findViewById(R.id.recycler) instanceof RecyclerView);
          });
    }
  }

  @Test
  public void notificationChannelsUseCorrectImportance() {
    android.content.Context c = InstrumentationRegistry.getInstrumentation().getTargetContext();
    NotificationHelper.createChannels(c);
    NotificationManager manager = c.getSystemService(NotificationManager.class);
    assertEquals(
        NotificationManager.IMPORTANCE_HIGH,
        manager.getNotificationChannel(NotificationHelper.HIGH).getImportance());
    assertEquals(
        NotificationManager.IMPORTANCE_DEFAULT,
        manager.getNotificationChannel(NotificationHelper.NORMAL).getImportance());
  }

  @Test
  public void emptyFormDoesNotFinish() {
    try (ActivityScenario<ExpenseFormActivity> scenario =
        ActivityScenario.launch(ExpenseFormActivity.class)) {
      scenario.onActivity(
          a -> {
            a.findViewById(R.id.saveButton).performClick();
            assertFalse(a.isFinishing());
          });
    }
  }
}
