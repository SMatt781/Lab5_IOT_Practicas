package com.example.lab5_practica2;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(tableName = "insumos")
public class InventoryItem {
  @PrimaryKey(autoGenerate = true)
  public long id;

  @NonNull public String name = "";
  public int quantity, minQuantity;
  @NonNull public String note = "";

  public InventoryItem() {}

  public static InventoryItem create(String name, int quantity, int minimum, String note) {
    InventoryItem item = new InventoryItem();
    item.name = name;
    item.quantity = quantity;
    item.minQuantity = minimum;
    item.note = note;
    return item;
  }

  public boolean low() {
    return quantity <= minQuantity;
  }
}
