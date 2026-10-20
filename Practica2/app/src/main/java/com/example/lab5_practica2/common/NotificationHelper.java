package com.example.lab5_practica2.common;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.*;
import androidx.core.content.ContextCompat;
import com.example.lab5_practica2.R;

public final class NotificationHelper {
  public static final String HIGH = "lab5_urgent", NORMAL = "lab5_regular";

  private NotificationHelper() {}

  public static void createChannels(Context context) {
    if (Build.VERSION.SDK_INT >= 26) {
      NotificationChannel high =
          new NotificationChannel(HIGH, "Stock bajo", NotificationManager.IMPORTANCE_HIGH);
      high.setDescription("Avisos que requieren atención");
      NotificationChannel normal =
          new NotificationChannel(
              NORMAL, "Movimientos de inventario", NotificationManager.IMPORTANCE_DEFAULT);
      context
          .getSystemService(NotificationManager.class)
          .createNotificationChannels(java.util.Arrays.asList(high, normal));
    }
  }

  public static boolean allowed(Context context) {
    return (Build.VERSION.SDK_INT < 33
            || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED)
        && NotificationManagerCompat.from(context).areNotificationsEnabled();
  }

  public static boolean show(
      Context context, String key, boolean urgent, String title, String text, Intent destination) {
    createChannels(context);
    if (!allowed(context)) return false;
    destination.setData(
        Uri.parse("lab5://" + context.getPackageName() + "/notification/" + Uri.encode(key)));
    PendingIntent tap =
        androidx.core.app.TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(destination)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context, urgent ? HIGH : NORMAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(
                urgent ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tap);
    try {
      NotificationManagerCompat.from(context).notify(key, 0, builder.build());
      return true;
    } catch (SecurityException denied) {
      return false;
    }
  }

  public static void cancel(Context context, String key) {
    NotificationManagerCompat.from(context).cancel(key, 0);
  }
}
