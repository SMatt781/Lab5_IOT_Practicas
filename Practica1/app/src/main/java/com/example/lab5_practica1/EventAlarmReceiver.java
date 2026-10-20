package com.example.lab5_practica1;

import android.content.*;

public class EventAlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent intent) {
    PendingResult result = goAsync();
    EventRepository.get(c)
        .deliver(
            intent.getIntExtra("id", -1),
            intent.getLongExtra("revision", -1),
            intent.getLongExtra("occurrence", 0),
            result::finish);
  }
}
