package com.example.lab5_practica2;

import android.content.*;
import android.os.*;
import androidx.lifecycle.LiveData;
import com.example.lab5_practica2.common.NotificationHelper;
import java.util.*;
import java.util.concurrent.*;

public final class InventoryRepository {
  public interface Done {
    void result(String error);
  }

  private static volatile InventoryRepository instance;
  private final Context context;
  private final InventoryDatabase db;
  private final InventoryDao dao;
  private final ExecutorService worker = Executors.newSingleThreadExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  public final LiveData<List<InventoryItem>> items;

  private InventoryRepository(Context c) {
    context = c.getApplicationContext();
    db = InventoryDatabase.get(c);
    dao = db.inventoryDao();
    items = dao.observeAll();
  }

  public static InventoryRepository get(Context c) {
    if (instance == null)
      synchronized (InventoryRepository.class) {
        if (instance == null) instance = new InventoryRepository(c);
      }
    return instance;
  }

  private interface Action {
    void run();
  }

  private void execute(Action action, Done callback) {
    worker.execute(
        () -> {
          String error = null;
          try {
            action.run();
          } catch (Exception ex) {
            error = ex.getMessage();
          }
          String result = error;
          main.post(() -> callback.result(result));
        });
  }

  public void save(InventoryItem item, Done done) {
    execute(
        () -> {
          String error = InventoryRules.validate(item.name, item.quantity, item.minQuantity);
          if (error != null) throw new IllegalArgumentException(error);
          if (item.note.length() > 300)
            throw new IllegalArgumentException("La nota admite hasta 300 caracteres");
          item.name = item.name.trim();
          InventoryItem before = item.id == 0 ? null : dao.find(item.id);
          if (item.id != 0 && before == null)
            throw new IllegalStateException("El insumo ya no existe");
          if (item.id == 0) item.id = dao.insert(item);
          else if (dao.update(item) != 1) throw new IllegalStateException("No se pudo actualizar");
          notifyChange(before, item);
        },
        done);
  }

  public void change(long id, int amount, Done done) {
    execute(
        () -> {
          InventoryItem[] before = new InventoryItem[1], after = new InventoryItem[1];
          db.runInTransaction(
              () -> {
                InventoryItem item = dao.find(id);
                if (item == null) throw new IllegalStateException("El insumo ya no existe");
                before[0] =
                    InventoryItem.create(item.name, item.quantity, item.minQuantity, item.note);
                before[0].id = id;
                int value = Math.addExact(item.quantity, amount);
                String error = InventoryRules.validate(item.name, value, item.minQuantity);
                if (error != null) throw new IllegalArgumentException(error);
                item.quantity = value;
                dao.update(item);
                after[0] = item;
              });
          notifyChange(before[0], after[0]);
        },
        done);
  }

  public void delete(InventoryItem item, Done done) {
    execute(
        () -> {
          dao.delete(item);
          NotificationHelper.cancel(context, "stock-" + item.id);
          NotificationHelper.cancel(context, "test-stock-" + item.id);
        },
        done);
  }

  public void examples(Done done) {
    execute(
        () -> {
          db.runInTransaction(
              () -> {
                if (dao.count() != 0)
                  throw new IllegalStateException(
                      "Los ejemplos solo se agregan con el inventario vacío");
                dao.insert(
                    InventoryItem.create("Sensor DHT22", 3, 5, "Mide temperatura y humedad"));
                dao.insert(
                    InventoryItem.create("Cables Dupont", 30, 10, "Conexiones de prototipos"));
                dao.insert(InventoryItem.create("ESP32", 6, 4, "Microcontrolador con Wi-Fi"));
                dao.insert(
                    InventoryItem.create("Protoboard", 0, 2, "Reponer antes de la práctica"));
              });
        },
        done);
  }

  private void notifyChange(InventoryItem before, InventoryItem item) {
    if (item.low() && (before == null || !before.low()))
      NotificationHelper.show(
          context,
          "stock-" + item.id,
          true,
          "Stock bajo: " + item.name,
          "Disponibles: " + item.quantity + ". Mínimo: " + item.minQuantity + ".",
          new Intent(context, ItemFormActivity.class).putExtra("item_id", item.id));
    else if (before != null && before.low() && !item.low()) {
      NotificationHelper.cancel(context, "stock-" + item.id);
      NotificationHelper.show(
          context,
          "stock-" + item.id,
          false,
          "Stock recuperado: " + item.name,
          "Ahora tienes " + item.quantity + " unidades.",
          new Intent(context, ItemFormActivity.class).putExtra("item_id", item.id));
    }
  }
}
