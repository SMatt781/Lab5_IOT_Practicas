package com.example.lab5_practica1;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica1.common.BaseActivity;
import com.example.lab5_practica1.databinding.ActivityEventFormBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.*;
import java.time.*;
import java.util.*;

public class EventFormActivity extends BaseActivity {
  private ActivityEventFormBinding b;
  private AgendaViewModel model;
  private String date;
  private int minutes = 540, period = 0, lead = 1, id;
  private boolean loaded;
  private final String[] periods = {"Una vez", "Cada año"},
      leads = {"Mismo día", "1 día antes (predeterminado)", "3 días antes", "1 semana antes"};
  private final int[] leadValues = {0, 1, 3, 7};

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityEventFormBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    backToolbar(b.toolbar);
    id = getIntent().getIntExtra("event_id", 0);
    if (state != null) {
      date = state.getString("date");
      minutes = state.getInt("minutes", 540);
      period = state.getInt("period");
      lead = state.getInt("lead", 1);
      loaded = state.getBoolean("loaded");
    }
    b.toolbar.setTitle(id == 0 ? "Añadir evento" : "Editar evento");
    b.periodInput.setSimpleItems(periods);
    b.leadInput.setSimpleItems(leads);
    b.periodInput.setOnItemClickListener((p, v, pos, item) -> period = pos);
    b.leadInput.setOnItemClickListener((p, v, pos, item) -> lead = leadValues[pos]);
    b.dateButton.setOnClickListener(
        v -> {
          MaterialDatePicker.Builder<Long> builder =
              MaterialDatePicker.Builder.datePicker().setTitleText("Fecha del evento");
          if (date != null)
            builder.setSelection(
                LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
          MaterialDatePicker<Long> picker = builder.build();
          picker.addOnPositiveButtonClickListener(
              value -> {
                date = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate().toString();
                renderChoices();
              });
          picker.show(getSupportFragmentManager(), "date");
        });
    b.timeSwitch.setOnCheckedChangeListener((v, checked) -> b.timeButton.setEnabled(checked));
    b.timeButton.setOnClickListener(
        v -> {
          MaterialTimePicker picker =
              new MaterialTimePicker.Builder()
                  .setTimeFormat(TimeFormat.CLOCK_24H)
                  .setHour(minutes / 60)
                  .setMinute(minutes % 60)
                  .setTitleText("Hora del evento")
                  .build();
          picker.addOnPositiveButtonClickListener(
              v2 -> {
                minutes = picker.getHour() * 60 + picker.getMinute();
                renderChoices();
              });
          picker.show(getSupportFragmentManager(), "time");
        });
    model = new ViewModelProvider(this).get(AgendaViewModel.class);
    model.repository.events.observe(
        this,
        list -> {
          if (id != 0 && !loaded)
            for (Event e : list)
              if (e.id == id) {
                b.nameEdit.setText(e.name);
                date = e.date;
                minutes = e.timeMinutes;
                period = e.annual ? 1 : 0;
                lead = e.leadDays;
                b.timeSwitch.setChecked(e.withTime);
                loaded = true;
                renderChoices();
                break;
              }
        });
    model.busy.observe(this, busy -> b.saveButton.setEnabled(!busy));
    model.result.observe(
        this,
        r -> {
          if (r != null && r.consume()) {
            message(r.message);
            if (r.success) finish();
          }
        });
    b.saveButton.setOnClickListener(v -> save());
    renderChoices();
  }

  private void renderChoices() {
    b.dateButton.setText(
        date == null
            ? "Seleccionar fecha (obligatoria)"
            : "Fecha: "
                + LocalDate.parse(date)
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    b.timeButton.setText(
        String.format(Locale.getDefault(), "Hora: %02d:%02d", minutes / 60, minutes % 60));
    b.timeButton.setEnabled(b.timeSwitch.isChecked());
    b.periodInput.setText(periods[period], false);
    for (int i = 0; i < leadValues.length; i++)
      if (leadValues[i] == lead) b.leadInput.setText(leads[i], false);
  }

  private void save() {
    if (id != 0 && !loaded) {
      message("Espera a que cargue el evento. Si fue eliminado, vuelve a la lista.");
      return;
    }
    Event e = new Event();
    e.id = id;
    e.name = b.nameEdit.getText().toString();
    e.date = date;
    e.withTime = b.timeSwitch.isChecked();
    e.timeMinutes = minutes;
    e.annual = period == 1;
    e.leadDays = lead;
    String error = EventDates.validate(e, LocalDateTime.now());
    b.nameEditLayout.setError(null);
    if (error != null) {
      if (e.name.trim().isEmpty()) b.nameEditLayout.setError(error);
      message(error);
      return;
    }
    model.save(e);
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putString("date", date);
    state.putInt("minutes", minutes);
    state.putInt("period", period);
    state.putInt("lead", lead);
    state.putBoolean("loaded", loaded);
    super.onSaveInstanceState(state);
  }
}
