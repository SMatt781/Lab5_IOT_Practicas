package com.example.lab5_practica1;

import android.app.Application;
import com.example.lab5_practica1.common.NotificationHelper;

public class AgendaApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    NotificationHelper.createChannels(this);
    EventRepository.get(this);
  }
}
