package com.example.lab5_practica3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica3.common.*;
import com.example.lab5_practica3.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainActivity extends BaseActivity implements ExpenseAdapter.Actions {
  private ActivityMainBinding b;
  private ExpenseViewModel model;
  private ExpenseAdapter adapter;
  private YearMonth month = YearMonth.now();
  private boolean budgetInitialized;
  private final ActivityResultLauncher<String> export =
      registerForActivityResult(
          new ActivityResultContracts.CreateDocument("application/json"),
          uri -> {
            if (uri != null) model.exportFile(uri);
          });
  private final ActivityResultLauncher<String[]> open =
      registerForActivityResult(
          new ActivityResultContracts.OpenDocument(),
          uri -> {
            if (uri != null)
              new MaterialAlertDialogBuilder(this)
                  .setTitle("¿Importar gastos?")
                  .setMessage(
                      "Se agregarán registros nuevos. Los que tengan el mismo ID se conservarán sin"
                          + " duplicarse. Tu presupuesto no cambia.")
                  .setNegativeButton("Cancelar", null)
                  .setPositiveButton("Importar", (d, w) -> model.importFile(uri))
                  .show();
          });

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityMainBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    if (state != null) {
      month = YearMonth.parse(state.getString("month"));
      budgetInitialized = state.getBoolean("budgetInitialized");
    } else readMonth(getIntent());
    model = new ViewModelProvider(this).get(ExpenseViewModel.class);
    adapter = new ExpenseAdapter(this);
    b.recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    b.recycler.setAdapter(adapter);
    b.previousButton.setOnClickListener(
        v -> {
          month = month.minusMonths(1);
          budgetInitialized = false;
          render();
        });
    b.nextButton.setOnClickListener(
        v -> {
          month = month.plusMonths(1);
          budgetInitialized = false;
          render();
        });
    b.permissionButton.setOnClickListener(v -> askNotifications());
    b.addButton.setOnClickListener(
        v ->
            startActivity(
                new Intent(this, ExpenseFormActivity.class).putExtra("month", month.toString())));
    b.budgetButton.setOnClickListener(
        v -> {
          try {
            b.budgetEditLayout.setError(null);
            model.budget(month.toString(), MoneyRules.parse(b.budgetEdit.getText().toString()));
          } catch (IllegalArgumentException ex) {
            b.budgetEditLayout.setError(ex.getMessage());
          }
        });
    b.exportButton.setOnClickListener(
        v -> export.launch("mis_gastos_" + LocalDate.now() + ".json"));
    b.importButton.setOnClickListener(
        v ->
            open.launch(
                new String[] {"application/json", "text/plain", "application/octet-stream"}));
    model.repository.items.observe(this, list -> render());
    model.repository.errors.observe(this, this::message);
    model.busy.observe(
        this,
        busy -> {
          b.budgetButton.setEnabled(!busy);
          b.importButton.setEnabled(!busy);
          b.exportButton.setEnabled(!busy);
          b.addButton.setEnabled(!busy);
        });
    model.result.observe(
        this,
        r -> {
          if (r != null && r.consume()) message(r.message);
        });
    render();
  }

  private void readMonth(Intent intent) {
    try {
      if (intent.hasExtra("month")) month = YearMonth.parse(intent.getStringExtra("month"));
    } catch (Exception ignored) {
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    readMonth(intent);
    budgetInitialized = false;
    render();
  }

  private void render() {
    List<Expense> all = model.repository.items.getValue();
    if (all == null) all = Collections.emptyList();
    String key = month.toString();
    long spent = ExpenseRepository.total(all, key), budget = model.repository.budget(key);
    List<Expense> visible = new ArrayList<>();
    for (Expense e : all) if (e.date.startsWith(key + "-")) visible.add(e);
    b.monthText.setText(
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "PE"))));
    b.summaryText.setText(
        "Gastado: "
            + MoneyRules.format(spent)
            + "\nPresupuesto: "
            + MoneyRules.format(budget)
            + "\nDisponible: "
            + MoneyRules.format(budget - spent));
    b.progress.setProgress((int) Math.min(100, spent * 100 / budget));
    if (!budgetInitialized) {
      b.budgetEdit.setText(MoneyRules.editable(budget));
      budgetInitialized = true;
    }
    b.emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    adapter.submit(visible);
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putString("month", month.toString());
    state.putBoolean("budgetInitialized", budgetInitialized);
    super.onSaveInstanceState(state);
  }

  @Override
  public void edit(Expense e) {
    startActivity(new Intent(this, ExpenseFormActivity.class).putExtra("expense_id", e.id));
  }

  @Override
  public void delete(Expense e) {
    new MaterialAlertDialogBuilder(this)
        .setTitle("¿Eliminar gasto?")
        .setMessage(e.title)
        .setNegativeButton("Volver", null)
        .setPositiveButton("Eliminar", (d, w) -> model.delete(e))
        .show();
  }

  @Override
  public void test(Expense e) {
    message(
        NotificationHelper.show(
                this,
                "expense-" + e.id,
                false,
                "Prueba: " + e.title,
                MoneyRules.format(e.cents) + " · " + e.date,
                new Intent(this, ExpenseFormActivity.class).putExtra("expense_id", e.id))
            ? "Aviso enviado"
            : "Permite las notificaciones y revisa los canales");
  }
}
