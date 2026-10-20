package com.example.lab5_practica3;

import android.app.Application;
import com.example.lab5_practica3.common.NotificationHelper;

public class BudgetApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    NotificationHelper.createChannels(this);
  }
}
