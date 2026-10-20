package com.example.lab5_practica1;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import java.time.*;

public final class AlarmScheduler {
  private AlarmScheduler() {}

  private static PendingIntent pending(
      Context c, int id, long revision, long occurrence, int flags) {
    Intent intent =
        new Intent(c, EventAlarmReceiver.class)
            .setAction("AGENDA_EVENT")
            .setData(Uri.parse("agenda://event/" + id))
            .putExtra("id", id)
            .putExtra("revision", revision)
            .putExtra("occurrence", occurrence);
    return PendingIntent.getBroadcast(c, 0, intent, flags | PendingIntent.FLAG_IMMUTABLE);
  }

  public static void cancel(Context c, int id) {
    PendingIntent old = pending(c, id, 0, 0, PendingIntent.FLAG_NO_CREATE);
    if (old != null) {
      c.getSystemService(AlarmManager.class).cancel(old);
      old.cancel();
    }
  }

  public static boolean exact(Context c) {
    return Build.VERSION.SDK_INT < 31
        || c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
  }

  public static void schedule(Context c, Event e) {
    cancel(c, e.id);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime at = EventDates.next(e, now);
    if (!e.annual && !at.isAfter(now)) return;
    long occurrence = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long last =
        c.getSharedPreferences("avisos_emitidos", 0).getLong("event-" + e.id, Long.MIN_VALUE);
    if (last == occurrence) {
      if (!e.annual) return;
      at = EventDates.atYear(e, at.getYear() + 1);
      occurrence = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    long trigger =
        at.minusDays(e.leadDays).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    trigger = Math.max(trigger, System.currentTimeMillis() + 1500);
    PendingIntent pi = pending(c, e.id, e.revision, occurrence, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarm = c.getSystemService(AlarmManager.class);
    if (exact(c)) {
      try {
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        return;
      } catch (SecurityException ignored) {
      }
    }
    alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
  }
}
