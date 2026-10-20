package com.example.lab5_practica1;

import android.content.*;

public class RestoreReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent intent) {
    PendingResult result = goAsync();
    EventRepository.get(c).refresh(result::finish);
  }
}
