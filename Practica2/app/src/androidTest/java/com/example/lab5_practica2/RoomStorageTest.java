package com.example.lab5_practica2;

import static org.junit.Assert.*;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RoomStorageTest {
  private InventoryDatabase db;

  @Before
  public void setup() {
    db =
        Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                InventoryDatabase.class)
            .build();
  }

  @After
  public void close() {
    db.close();
  }

  @Test
  public void insertReadUpdateDelete() {
    InventoryDao dao = db.inventoryDao();
    InventoryItem i = InventoryItem.create("DHT22", 3, 2, "Prueba");
    i.id = dao.insert(i);
    assertEquals(1, dao.count());
    assertEquals(3, dao.find(i.id).quantity);
    i.quantity = 1;
    assertEquals(1, dao.update(i));
    assertTrue(dao.find(i.id).low());
    assertEquals(1, dao.delete(i));
    assertNull(dao.find(i.id));
  }

  @Test
  public void transactionRollsBackOnFailure() {
    try {
      db.runInTransaction(
          () -> {
            db.inventoryDao().insert(InventoryItem.create("Temporal", 1, 1, ""));
            throw new IllegalStateException("Revertir");
          });
    } catch (IllegalStateException expected) {
    }
    assertEquals(0, db.inventoryDao().count());
  }
}
