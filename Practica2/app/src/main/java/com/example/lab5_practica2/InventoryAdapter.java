package com.example.lab5_practica2;

import android.view.*;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab5_practica2.databinding.RowInventoryBinding;
import java.util.*;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.Holder> {
  public interface Actions {
    void edit(InventoryItem i);

    void delete(InventoryItem i);

    void change(InventoryItem i, int delta);

    void test(InventoryItem i);
  }

  private List<InventoryItem> items = new ArrayList<>();
  private final Actions actions;

  public InventoryAdapter(Actions a) {
    actions = a;
  }

  public void submit(List<InventoryItem> list) {
    items = new ArrayList<>(list);
    notifyDataSetChanged();
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup parent, int type) {
    return new Holder(
        RowInventoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  @Override
  public void onBindViewHolder(Holder h, int pos) {
    InventoryItem i = items.get(pos);
    h.b.nameText.setText(i.name);
    h.b.stockText.setText("Disponibles: " + i.quantity + " · Mínimo: " + i.minQuantity);
    h.b.statusText.setText(i.low() ? "STOCK BAJO" : "STOCK SUFICIENTE");
    h.b.statusText.setTextColor(
        h.itemView.getContext().getColor(i.low() ? R.color.lab_error : R.color.lab_primary));
    h.b.noteText.setText(i.note);
    h.b.removeButton.setEnabled(i.quantity > 0);
    h.b.removeButton.setOnClickListener(v -> actions.change(i, -1));
    h.b.addButton.setOnClickListener(v -> actions.change(i, 1));
    h.b.editButton.setOnClickListener(v -> actions.edit(i));
    h.b.deleteButton.setOnClickListener(v -> actions.delete(i));
    h.b.testButton.setOnClickListener(v -> actions.test(i));
  }

  static class Holder extends RecyclerView.ViewHolder {
    final RowInventoryBinding b;

    Holder(RowInventoryBinding b) {
      super(b.getRoot());
      this.b = b;
    }
  }
}
