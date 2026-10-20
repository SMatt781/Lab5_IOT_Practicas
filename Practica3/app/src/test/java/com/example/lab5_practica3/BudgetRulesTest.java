package com.example.lab5_practica3;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class BudgetRulesTest {
  private Expense example() {
    Expense e = new Expense();
    e.id = UUID.randomUUID().toString();
    e.title = "Almuerzo";
    e.category = "Alimentación";
    e.note = "";
    e.date = "2026-10-20";
    e.cents = 1250;
    return e;
  }

  @Test
  public void commaAndPeriodWork() {
    assertEquals(1250, MoneyRules.parse("12,50"));
    assertEquals(1250, MoneyRules.parse("12.50"));
  }

  @Test
  public void centsRemainExact() {
    assertEquals(10, MoneyRules.parse("0.10"));
    assertEquals("0.10", MoneyRules.editable(10));
  }

  @Test(expected = IllegalArgumentException.class)
  public void negativeRejected() {
    MoneyRules.parse("-2");
  }

  @Test(expected = IllegalArgumentException.class)
  public void zeroRejected() {
    MoneyRules.parse("0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void extraDecimalsRejected() {
    MoneyRules.parse("1.234");
  }

  @Test
  public void thresholdLevels() {
    assertEquals(0, MoneyRules.level(7999, 10000));
    assertEquals(1, MoneyRules.level(8000, 10000));
    assertEquals(2, MoneyRules.level(10000, 10000));
  }

  @Test
  public void jsonRoundTrip() {
    Expense e = example();
    List<Expense> list = LedgerCodec.decode(LedgerCodec.encode(Arrays.asList(e)));
    assertEquals(e.id, list.get(0).id);
    assertEquals(1250, list.get(0).cents);
  }

  @Test(expected = IllegalArgumentException.class)
  public void alienJsonRejected() {
    LedgerCodec.decode("{}");
  }

  @Test(expected = IllegalArgumentException.class)
  public void duplicateIdsRejected() {
    Expense e = example();
    LedgerCodec.decode(LedgerCodec.encode(Arrays.asList(e, e)));
  }

  @Test
  public void importIdempotent() {
    Expense e = example();
    List<Expense> merged = LedgerCodec.merge(Arrays.asList(e), Arrays.asList(e));
    assertEquals(1, merged.size());
  }

  @Test
  public void mergePreservesExistingRecord() {
    Expense first = example(), incoming = example();
    incoming.id = first.id;
    incoming.title = "Cambiado";
    assertEquals(
        "Almuerzo", LedgerCodec.merge(Arrays.asList(first), Arrays.asList(incoming)).get(0).title);
  }

  @Test
  public void invalidDateRejected() {
    Expense e = example();
    e.date = "2026-02-30";
    assertNotNull(MoneyRules.validate(e));
  }
}
