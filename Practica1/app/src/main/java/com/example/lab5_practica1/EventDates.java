package com.example.lab5_practica1;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class EventDates {
  private EventDates() {}

  public static LocalDateTime atYear(Event e, int year) {
    LocalDate original = LocalDate.parse(e.date);
    int day =
        Math.min(
            original.getDayOfMonth(), YearMonth.of(year, original.getMonthValue()).lengthOfMonth());
    return LocalDate.of(year, original.getMonthValue(), day)
        .atStartOfDay()
        .plusMinutes(e.withTime ? e.timeMinutes : 540);
  }

  public static LocalDateTime next(Event e, LocalDateTime from) {
    if (!e.annual)
      return LocalDate.parse(e.date).atStartOfDay().plusMinutes(e.withTime ? e.timeMinutes : 540);
    LocalDateTime candidate = atYear(e, from.getYear());
    return candidate.isBefore(from) ? atYear(e, from.getYear() + 1) : candidate;
  }

  public static String label(Event e, LocalDateTime date) {
    return date.format(DateTimeFormatter.ofPattern(e.withTime ? "dd/MM/yyyy HH:mm" : "dd/MM/yyyy"))
        + (e.withTime ? "" : " · aviso a las 09:00");
  }

  public static String remaining(Event e) {
    LocalDateTime now = LocalDateTime.now();
    return remaining(next(e, now), now);
  }

  public static String remaining(LocalDateTime occurrence, LocalDateTime now) {
    if (occurrence.isBefore(now)) return "Ya ocurrió";
    long minutes = Duration.between(now, occurrence).toMinutes();
    return "Faltan " + (minutes / 1440) + " días y " + ((minutes % 1440) / 60) + " horas";
  }

  public static String validate(Event e, LocalDateTime now) {
    if (e.name == null || e.name.trim().isEmpty()) return "Escribe el nombre del evento";
    if (e.name.trim().length() > 100) return "El nombre admite hasta 100 caracteres";
    try {
      LocalDate.parse(e.date);
    } catch (Exception error) {
      return "Selecciona una fecha";
    }
    if (e.timeMinutes < 0 || e.timeMinutes > 1439) return "Hora no válida";
    if (e.leadDays != 0 && e.leadDays != 1 && e.leadDays != 3 && e.leadDays != 7)
      return "Anticipación no válida";
    if (!e.annual && !next(e, now).isAfter(now)) return "El evento único debe ocurrir en el futuro";
    return null;
  }
}
