package com.example.lab5_practica1;

import static org.junit.Assert.*;

import java.time.*;
import org.junit.Test;

public class EventDatesTest {
  private Event event(String date, boolean annual) {
    Event e = new Event();
    e.name = "Examen";
    e.date = date;
    e.annual = annual;
    return e;
  }

  @Test
  public void uniqueKeepsOriginalDate() {
    assertEquals("Faltan 1 días y 2 horas",
        EventDates.remaining(LocalDateTime.of(2030, 1, 2, 11, 0), LocalDateTime.of(2030, 1, 1, 9, 0)));
    assertEquals("Ya ocurrió",
        EventDates.remaining(LocalDateTime.of(2030, 1, 1, 8, 59, 59), LocalDateTime.of(2030, 1, 1, 9, 0)));
    assertEquals(
        LocalDateTime.of(2027, 1, 2, 9, 0),
        EventDates.next(event("2027-01-02", false), LocalDateTime.of(2026, 10, 20, 12, 0)));
  }

  @Test
  public void annualMovesToNextYear() {
    assertEquals(
        2027,
        EventDates.next(event("2000-01-02", true), LocalDateTime.of(2026, 10, 20, 12, 0))
            .getYear());
  }

  @Test
  public void leapBirthdayClampsOnlyNonLeapYear() {
    Event e = event("2000-02-29", true);
    assertEquals(28, EventDates.atYear(e, 2027).getDayOfMonth());
    assertEquals(29, EventDates.atYear(e, 2028).getDayOfMonth());
  }

  @Test
  public void explicitHourIsUsed() {
    Event e = event("2027-01-02", false);
    e.withTime = true;
    e.timeMinutes = 14 * 60 + 35;
    assertEquals(LocalTime.of(14, 35), EventDates.next(e, LocalDateTime.now()).toLocalTime());
  }

  @Test
  public void pastUniqueRejected() {
    assertNotNull(
        EventDates.validate(event("2020-01-02", false), LocalDateTime.of(2026, 10, 20, 12, 0)));
  }

  @Test
  public void annualPastBirthYearAccepted() {
    assertNull(
        EventDates.validate(event("2000-01-02", true), LocalDateTime.of(2026, 10, 20, 12, 0)));
  }

  @Test
  public void missingFieldsRejected() {
    Event e = event(null, false);
    assertNotNull(EventDates.validate(e, LocalDateTime.now()));
    e.date = "2027-05-01";
    e.name = "  ";
    assertNotNull(EventDates.validate(e, LocalDateTime.now()));
  }

  @Test
  public void unsupportedLeadRejected() {
    Event e = event("2030-05-01", false);
    e.leadDays = 2;
    assertNotNull(EventDates.validate(e, LocalDateTime.now()));
  }
}
