package com.example.lab5_practica2;

import android.app.Application;
import com.example.lab5_practica2.common.NotificationHelper;

public class InventoryApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    NotificationHelper.createChannels(this);
  }
}
