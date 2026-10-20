package com.example.lab5_practica3;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JsonStorageTest {
  @Test
  public void atomicFilePersistsWhenStoreIsRecreated() throws Exception {
    android.content.Context c = InstrumentationRegistry.getInstrumentation().getTargetContext();
    ExpenseStore store = new ExpenseStore(c);
    List<Expense> original = store.read();
    Expense e = new Expense();
    e.id = UUID.randomUUID().toString();
    e.title = "Prueba JSON";
    e.date = "2026-10-20";
    e.category = "Otros";
    e.note = "";
    e.cents = 55;
    try {
      List<Expense> list = new ArrayList<>(original);
      list.add(e);
      store.write(list);
      List<Expense> restored = new ExpenseStore(c).read();
      assertTrue(restored.stream().anyMatch(x -> x.id.equals(e.id) && x.cents == 55));
    } finally {
      store.write(original);
    }
  }

  @Test
  public void invalidImportDoesNotOverwriteExistingData() throws Exception {
    ExpenseStore store =
        new ExpenseStore(InstrumentationRegistry.getInstrumentation().getTargetContext());
    String before = LedgerCodec.encode(store.read());
    try {
      List<Expense> incoming = LedgerCodec.decode("{\"version\":99,\"expenses\":[]}");
      store.write(incoming);
      fail("Debió rechazar");
    } catch (IllegalArgumentException expected) {
    }
    assertEquals(before, LedgerCodec.encode(store.read()));
  }
}
