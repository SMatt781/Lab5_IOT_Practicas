package com.example.lab5_practica2;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica2.common.BaseActivity;
import com.example.lab5_practica2.databinding.ActivityItemFormBinding;

public class ItemFormActivity extends BaseActivity {
  private ActivityItemFormBinding b;
  private InventoryViewModel model;
  private long id;
  private boolean loaded;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityItemFormBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    backToolbar(b.toolbar);
    id = getIntent().getLongExtra("item_id", 0);
    loaded = state != null && state.getBoolean("loaded");
    b.toolbar.setTitle(id == 0 ? "Registrar insumo" : "Editar insumo");
    if (id == 0 && state == null) {
      b.quantityEdit.setText("0");
      b.minimumEdit.setText("1");
    }
    model = new ViewModelProvider(this).get(InventoryViewModel.class);
    model.repository.items.observe(
        this,
        list -> {
          if (id != 0 && !loaded)
            for (InventoryItem i : list)
              if (i.id == id) {
                b.nameEdit.setText(i.name);
                b.quantityEdit.setText(String.valueOf(i.quantity));
                b.minimumEdit.setText(String.valueOf(i.minQuantity));
                b.noteEdit.setText(i.note);
                loaded = true;
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
  }

  private void save() {
    if (id != 0 && !loaded) {
      message("El insumo no cargó o fue eliminado. Vuelve a la lista.");
      return;
    }
    b.nameEditLayout.setError(null);
    b.quantityEditLayout.setError(null);
    b.minimumEditLayout.setError(null);
    try {
      String name = b.nameEdit.getText().toString();
      int quantity = InventoryRules.parse(b.quantityEdit.getText().toString()),
          minimum = InventoryRules.parse(b.minimumEdit.getText().toString());
      String error = InventoryRules.validate(name, quantity, minimum);
      if (error != null) {
        b.nameEditLayout.setError(error);
        return;
      }
      InventoryItem i =
          InventoryItem.create(name, quantity, minimum, b.noteEdit.getText().toString().trim());
      i.id = id;
      model.save(i);
    } catch (IllegalArgumentException ex) {
      message(ex.getMessage());
      b.quantityEditLayout.setError("Revisa cantidad y mínimo: solo enteros de 0 a 1000000");
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putBoolean("loaded", loaded);
    super.onSaveInstanceState(state);
  }
}
