package com.example.lab5_practica2;

import android.content.Context;
import androidx.room.*;

@Database(
    entities = {InventoryItem.class},
    version = 1,
    exportSchema = false)
public abstract class InventoryDatabase extends RoomDatabase {
  public abstract InventoryDao inventoryDao();

  private static volatile InventoryDatabase instance;

  public static InventoryDatabase get(Context c) {
    if (instance == null)
      synchronized (InventoryDatabase.class) {
        if (instance == null)
          instance =
              Room.databaseBuilder(
                      c.getApplicationContext(), InventoryDatabase.class, "inventario.db")
                  .build();
      }
    return instance;
  }
}
