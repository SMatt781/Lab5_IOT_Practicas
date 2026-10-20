package com.example.lab5_practica2;

public final class InventoryRules {
  private InventoryRules() {}

  public static String validate(String name, int quantity, int minimum) {
    if (name == null || name.trim().isEmpty()) return "Escribe el nombre del insumo";
    if (name.trim().length() > 80) return "El nombre admite hasta 80 caracteres";
    if (quantity < 0 || minimum < 0) return "Las cantidades no pueden ser negativas";
    if (quantity > 1000000 || minimum > 1000000) return "El máximo permitido es 1000000";
    return null;
  }

  public static int parse(String value) {
    if (value == null || !value.matches("[0-9]{1,7}"))
      throw new IllegalArgumentException("Escribe cantidades enteras, entre 0 y 1000000");
    int n = Integer.parseInt(value);
    if (n > 1000000) throw new IllegalArgumentException("El máximo es 1000000");
    return n;
  }
}
