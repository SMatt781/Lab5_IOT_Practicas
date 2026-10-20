package com.example.lab5_practica3;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica3.common.BaseActivity;
import com.example.lab5_practica3.databinding.ActivityExpenseFormBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.time.*;
import java.util.*;

public class ExpenseFormActivity extends BaseActivity {
  private ActivityExpenseFormBinding b;
  private ExpenseViewModel model;
  private String id, date = LocalDate.now().toString(), category = "Otros";
  private boolean edit, loaded;
  private final String[] categories = {"Alimentación", "Transporte", "Estudios", "Hogar", "Otros"};

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityExpenseFormBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    backToolbar(b.toolbar);
    edit = getIntent().hasExtra("expense_id");
    id = edit ? getIntent().getStringExtra("expense_id") : UUID.randomUUID().toString();
    if (state != null) {
      id = state.getString("id");
      date = state.getString("date");
      category = state.getString("category");
      loaded = state.getBoolean("loaded");
    } else if (!edit && getIntent().hasExtra("month")) {
      try {
        YearMonth m = YearMonth.parse(getIntent().getStringExtra("month"));
        date = m.equals(YearMonth.now()) ? LocalDate.now().toString() : m.atDay(1).toString();
      } catch (Exception ignored) {
      }
    }
    b.toolbar.setTitle(edit ? "Editar gasto" : "Registrar gasto");
    b.categoryInput.setSimpleItems(categories);
    b.categoryInput.setOnItemClickListener((p, v, pos, item) -> category = categories[pos]);
    b.dateButton.setOnClickListener(
        v -> {
          MaterialDatePicker<Long> picker =
              MaterialDatePicker.Builder.datePicker()
                  .setTitleText("Fecha del gasto")
                  .setSelection(
                      LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                  .build();
          picker.addOnPositiveButtonClickListener(
              value -> {
                date = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate().toString();
                render();
              });
          picker.show(getSupportFragmentManager(), "date");
        });
    model = new ViewModelProvider(this).get(ExpenseViewModel.class);
    model.repository.items.observe(
        this,
        list -> {
          if (edit && !loaded)
            for (Expense e : list)
              if (e.id.equals(id)) {
                b.titleEdit.setText(e.title);
                b.amountEdit.setText(MoneyRules.editable(e.cents));
                date = e.date;
                category = e.category;
                b.noteEdit.setText(e.note);
                loaded = true;
                render();
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
    render();
  }

  private void render() {
    b.dateButton.setText("Fecha: " + date);
    b.categoryInput.setText(category, false);
  }

  private void save() {
    if (edit && !loaded) {
      message("El gasto no cargó o fue eliminado. Vuelve al resumen.");
      return;
    }
    b.titleEditLayout.setError(null);
    b.amountEditLayout.setError(null);
    try {
      Expense e = new Expense();
      e.id = id;
      e.date = date;
      e.category = category;
      e.title = b.titleEdit.getText().toString().trim();
      e.note = b.noteEdit.getText().toString().trim();
      e.cents = MoneyRules.parse(b.amountEdit.getText().toString());
      String error = MoneyRules.validate(e);
      if (error != null) {
        b.titleEditLayout.setError(error);
        return;
      }
      model.save(e);
    } catch (IllegalArgumentException ex) {
      b.amountEditLayout.setError(ex.getMessage());
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putString("id", id);
    state.putString("date", date);
    state.putString("category", category);
    state.putBoolean("loaded", loaded);
    super.onSaveInstanceState(state);
  }
}
