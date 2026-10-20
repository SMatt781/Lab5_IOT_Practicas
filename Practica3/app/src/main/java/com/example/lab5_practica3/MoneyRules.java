package com.example.lab5_practica3;

import java.math.BigDecimal;
import java.util.Locale;

public final class MoneyRules {
  private MoneyRules() {}

  public static long parse(String text) {
    String s = text == null ? "" : text.trim().replace(',', '.');
    if (!s.matches("[0-9]{1,7}([.][0-9]{1,2})?"))
      throw new IllegalArgumentException("Escribe un importe positivo con máximo dos decimales");
    long value = new BigDecimal(s).movePointRight(2).longValueExact();
    if (value <= 0 || value > 999999999)
      throw new IllegalArgumentException("El importe debe estar entre 0.01 y 9999999.99");
    return value;
  }

  public static String format(long cents) {
    return String.format(new Locale("es", "PE"), "S/ %,.2f", cents / 100.0);
  }

  public static String editable(long cents) {
    return BigDecimal.valueOf(cents, 2).toPlainString();
  }

  public static int level(long spent, long budget) {
    if (budget <= 0) return 0;
    if (spent >= budget) return 2;
    return spent * 100 >= budget * 80 ? 1 : 0;
  }

  public static String validate(Expense e) {
    if (e == null || e.id == null || e.id.isEmpty()) return "Registro sin identificador";
    try {
      java.util.UUID.fromString(e.id);
      java.time.LocalDate.parse(e.date);
    } catch (Exception ex) {
      return "Identificador o fecha no válida";
    }
    if (e.title == null || e.title.trim().isEmpty() || e.title.length() > 100)
      return "El concepto debe tener de 1 a 100 caracteres";
    if (e.cents <= 0 || e.cents > 999999999) return "Importe fuera del rango permitido";
    if (!java.util.Arrays.asList("Alimentación", "Transporte", "Estudios", "Hogar", "Otros")
        .contains(e.category)) return "Categoría no válida";
    if (e.note == null || e.note.length() > 300) return "La nota admite hasta 300 caracteres";
    return null;
  }
}
