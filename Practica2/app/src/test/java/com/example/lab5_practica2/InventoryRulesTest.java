package com.example.lab5_practica2;

import static org.junit.Assert.*;

import org.junit.Test;

public class InventoryRulesTest {
  @Test
  public void zeroAccepted() {
    assertNull(InventoryRules.validate("DHT22", 0, 1));
  }

  @Test
  public void blankRejected() {
    assertNotNull(InventoryRules.validate("  ", 1, 1));
  }

  @Test
  public void negativeRejected() {
    assertNotNull(InventoryRules.validate("ESP32", -1, 0));
  }

  @Test
  public void tooLargeRejected() {
    assertNotNull(InventoryRules.validate("ESP32", 1000001, 0));
  }

  @Test
  public void parseInteger() {
    assertEquals(15, InventoryRules.parse("15"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void fractionRejected() {
    InventoryRules.parse("1.5");
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyRejected() {
    InventoryRules.parse("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void overflowRejected() {
    InventoryRules.parse("99999999999");
  }
}
