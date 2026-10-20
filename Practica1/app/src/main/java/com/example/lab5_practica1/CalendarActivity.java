package com.example.lab5_practica1;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica1.common.*;
import com.example.lab5_practica1.databinding.ActivityCalendarBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class CalendarActivity extends BaseActivity implements EventAdapter.Actions {
  private ActivityCalendarBinding b;
  private AgendaViewModel model;
  private EventAdapter adapter;
  private YearMonth month = YearMonth.now();
  private LocalDate selected = LocalDate.now();

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityCalendarBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    backToolbar(b.toolbar);
    if (state != null) {
      month = YearMonth.parse(state.getString("month"));
      selected = LocalDate.parse(state.getString("selected"));
    }
    model = new ViewModelProvider(this).get(AgendaViewModel.class);
    adapter = new EventAdapter(this);
    b.recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    b.recycler.setAdapter(adapter);
    b.previousButton.setOnClickListener(
        v -> {
          month = month.minusMonths(1);
          render();
        });
    b.nextButton.setOnClickListener(
        v -> {
          month = month.plusMonths(1);
          render();
        });
    model.repository.events.observe(this, items -> render());
    model.result.observe(
        this,
        r -> {
          if (r != null && r.consume()) message(r.message);
        });
  }

  private void render() {
    List<Event> list = model.repository.events.getValue();
    if (list == null) list = Collections.emptyList();
    b.monthText.setText(
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "PE"))));
    b.calendarGrid.removeAllViews();
    int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
    for (int i = 0; i < 42; i++) {
      int day = i - offset + 1;
      TextView cell = new TextView(this);
      GridLayout.LayoutParams lp =
          new GridLayout.LayoutParams(GridLayout.spec(i / 7), GridLayout.spec(i % 7, 1f));
      lp.width = 0;
      lp.height = (int) (44 * getResources().getDisplayMetrics().density);
      cell.setLayoutParams(lp);
      cell.setGravity(Gravity.CENTER);
      cell.setTextSize(14);
      cell.setTextColor(getColor(R.color.lab_text));
      if (day > 0 && day <= month.lengthOfMonth()) {
        LocalDate d = month.atDay(day);
        boolean annual = false, once = false;
        for (Event e : list) {
          LocalDate at =
              e.annual
                  ? EventDates.atYear(e, month.getYear()).toLocalDate()
                  : LocalDate.parse(e.date);
          if (at.equals(d)) {
            if (e.annual) annual = true;
            else once = true;
          }
        }
        SpannableString label =
            new SpannableString(day + "\n" + (annual ? "●" : " ") + (once ? "●" : " "));
        int start = label.toString().indexOf('\n') + 1;
        label.setSpan(new ForegroundColorSpan(getColor(R.color.lab_high)), start, start + 1, 0);
        label.setSpan(
            new ForegroundColorSpan(getColor(R.color.lab_normal)), start + 1, start + 2, 0);
        cell.setText(label);
        if (d.equals(selected)) {
          cell.setBackgroundColor(0xFFDDEEEA);
          cell.setTypeface(null, Typeface.BOLD);
        }
        cell.setContentDescription(
            d.toString() + (annual ? " evento anual" : "") + (once ? " evento único" : ""));
        cell.setOnClickListener(
            v -> {
              selected = d;
              render();
            });
      }
      b.calendarGrid.addView(cell);
    }
    List<Event> future = new ArrayList<>();
    for (Event e : list)
      if (!EventDates.next(e, selected.atStartOfDay()).isBefore(selected.atStartOfDay()))
        future.add(e);
    b.selectedText.setText(
        "Eventos desde " + selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    b.emptyText.setVisibility(future.isEmpty() ? View.VISIBLE : View.GONE);
    adapter.submit(future, selected.atStartOfDay());
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putString("month", month.toString());
    state.putString("selected", selected.toString());
    super.onSaveInstanceState(state);
  }

  @Override
  public void edit(Event e) {
    startActivity(new Intent(this, EventFormActivity.class).putExtra("event_id", e.id));
  }

  @Override
  public void delete(Event e) {
    new MaterialAlertDialogBuilder(this)
        .setTitle("¿Eliminar evento?")
        .setMessage(e.name)
        .setNegativeButton("Volver", null)
        .setPositiveButton("Eliminar", (d, w) -> model.delete(e))
        .show();
  }

  @Override
  public void test(Event e) {
    message(
        NotificationHelper.show(
                this,
                "test-event-" + e.id,
                e.annual,
                "Prueba: " + e.name,
                EventDates.label(e, EventDates.next(e, LocalDateTime.now())),
                new Intent(this, EventFormActivity.class).putExtra("event_id", e.id))
            ? "Aviso enviado"
            : "Permite las notificaciones desde Inicio");
  }
}
