package com.example.lab5_practica3;

import android.content.*;
import android.net.Uri;
import android.os.*;
import androidx.lifecycle.*;
import com.example.lab5_practica3.common.NotificationHelper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.*;

public final class ExpenseRepository {
  public interface Done {
    void result(String error);
  }

  private static volatile ExpenseRepository instance;
  private final Context context;
  private final ExpenseStore store;
  private final android.content.SharedPreferences budgets;
  private final ExecutorService worker = Executors.newSingleThreadExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  public final MutableLiveData<List<Expense>> items = new MutableLiveData<>(new ArrayList<>());
  public final MutableLiveData<String> errors = new MutableLiveData<>();

  private ExpenseRepository(Context c) {
    context = c.getApplicationContext();
    store = new ExpenseStore(context);
    budgets = context.getSharedPreferences("presupuestos", 0);
    refresh();
  }

  public static ExpenseRepository get(Context c) {
    if (instance == null)
      synchronized (ExpenseRepository.class) {
        if (instance == null) instance = new ExpenseRepository(c);
      }
    return instance;
  }

  private interface Work {
    void run() throws Exception;
  }

  private void execute(Work action, Done done) {
    worker.execute(
        () -> {
          String error = null;
          try {
            action.run();
          } catch (Exception ex) {
            error = ex.getMessage();
          }
          String result = error;
          main.post(() -> done.result(result));
        });
  }

  public void refresh() {
    worker.execute(
        () -> {
          try {
            items.postValue(store.read());
          } catch (Exception ex) {
            errors.postValue("No se pudo leer gastos.json: " + ex.getMessage());
          }
        });
  }

  public long budget(String month) {
    return budgets.getLong("budget_" + month, 50000);
  }

  public static long total(List<Expense> list, String month) {
    long sum = 0;
    for (Expense e : list) if (e.date.startsWith(month + "-")) sum = Math.addExact(sum, e.cents);
    return sum;
  }

  public void save(Expense e, Done done) {
    execute(
        () -> {
          String error = MoneyRules.validate(e);
          if (error != null) throw new IllegalArgumentException(error);
          List<Expense> before = store.read(), after = new ArrayList<>(before);
          boolean edit = after.removeIf(x -> x.id.equals(e.id));
          after.add(e);
          if (after.size() > 10000) throw new IllegalArgumentException("Máximo 10000 gastos");
          store.write(after);
          items.postValue(after);
          if (!edit)
            NotificationHelper.show(
                context,
                "expense-" + e.id,
                false,
                "Gasto registrado",
                e.title + ": " + MoneyRules.format(e.cents),
                new Intent(context, ExpenseFormActivity.class).putExtra("expense_id", e.id));
          changedBudgets(before, after);
        },
        done);
  }

  public void delete(Expense e, Done done) {
    execute(
        () -> {
          List<Expense> before = store.read(), after = new ArrayList<>(before);
          after.removeIf(x -> x.id.equals(e.id));
          store.write(after);
          items.postValue(after);
          NotificationHelper.cancel(context, "expense-" + e.id);
          changedBudgets(before, after);
        },
        done);
  }

  public void setBudget(String month, long value, Done done) {
    execute(
        () -> {
          YearMonth.parse(month);
          if (value <= 0 || value > 999999999)
            throw new IllegalArgumentException("Presupuesto no válido");
          List<Expense> list = store.read();
          long spent = total(list, month), old = budget(month);
          if (!budgets.edit().putLong("budget_" + month, value).commit())
            throw new IOException("No se pudo guardar el presupuesto");
          notifyBudget(month, spent, old, spent, value);
          items.postValue(list);
        },
        done);
  }

  private void changedBudgets(List<Expense> before, List<Expense> after) {
    Set<String> months = new HashSet<>();
    for (Expense e : before) months.add(e.date.substring(0, 7));
    for (Expense e : after) months.add(e.date.substring(0, 7));
    for (String month : months)
      notifyBudget(month, total(before, month), budget(month), total(after, month), budget(month));
  }

  private void notifyBudget(String month, long oldSpent, long oldBudget, long spent, long budget) {
    int old = MoneyRules.level(oldSpent, oldBudget), level = MoneyRules.level(spent, budget);
    if (old == level) return;
    NotificationHelper.cancel(context, "budget-" + month);
    String title =
        level == 2
            ? "Presupuesto alcanzado o superado"
            : level == 1 ? "Ya usaste al menos el 80%" : "Presupuesto nuevamente disponible";
    NotificationHelper.show(
        context,
        "budget-" + month,
        level == 2,
        title,
        month + ": " + MoneyRules.format(spent) + " de " + MoneyRules.format(budget),
        new Intent(context, MainActivity.class).putExtra("month", month));
  }

  public void exportFile(Uri uri, Done done) {
    execute(
        () -> {
          String json = LedgerCodec.encode(store.read());
          try (OutputStream out = context.getContentResolver().openOutputStream(uri, "wt")) {
            if (out == null) throw new IOException("No se pudo abrir el destino");
            out.write(json.getBytes(StandardCharsets.UTF_8));
          }
        },
        done);
  }

  public void importFile(Uri uri, Done done) {
    execute(
        () -> {
          List<Expense> incoming;
          try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("No se pudo abrir el archivo");
            incoming = LedgerCodec.decode(ExpenseStore.readText(in));
          }
          List<Expense> before = store.read(), after = LedgerCodec.merge(before, incoming);
          store.write(after);
          items.postValue(after);
          changedBudgets(before, after);
        },
        done);
  }
}
