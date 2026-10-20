package com.example.lab5_practica3;

import android.view.*;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab5_practica3.databinding.RowExpenseBinding;
import java.util.*;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.Holder> {
  public interface Actions {
    void edit(Expense e);

    void delete(Expense e);

    void test(Expense e);
  }

  private List<Expense> items = new ArrayList<>();
  private final Actions actions;

  public ExpenseAdapter(Actions a) {
    actions = a;
  }

  public void submit(List<Expense> list) {
    items = new ArrayList<>(list);
    items.sort(Comparator.comparing((Expense e) -> e.date).reversed().thenComparing(e -> e.title));
    notifyDataSetChanged();
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup p, int type) {
    return new Holder(RowExpenseBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  @Override
  public void onBindViewHolder(Holder h, int pos) {
    Expense e = items.get(pos);
    h.b.titleText.setText(e.title);
    h.b.amountText.setText(MoneyRules.format(e.cents));
    h.b.detailText.setText(e.date + " · " + e.category);
    h.b.noteText.setText(e.note);
    h.b.editButton.setOnClickListener(v -> actions.edit(e));
    h.b.deleteButton.setOnClickListener(v -> actions.delete(e));
    h.b.testButton.setOnClickListener(v -> actions.test(e));
  }

  static class Holder extends RecyclerView.ViewHolder {
    final RowExpenseBinding b;

    Holder(RowExpenseBinding b) {
      super(b.getRoot());
      this.b = b;
    }
  }
}
