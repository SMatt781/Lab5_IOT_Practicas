package com.example.lab5_practica2;

import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab5_practica2.common.*;
import com.example.lab5_practica2.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.*;

public class MainActivity extends BaseActivity implements InventoryAdapter.Actions {
  private ActivityMainBinding b;
  private InventoryViewModel model;
  private InventoryAdapter adapter;
  private android.content.SharedPreferences prefs;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    b = ActivityMainBinding.inflate(getLayoutInflater());
    showContent(b.getRoot());
    model = new ViewModelProvider(this).get(InventoryViewModel.class);
    prefs = getSharedPreferences("inventario_config", MODE_PRIVATE);
    b.lowSwitch.setChecked(prefs.getBoolean("solo_bajo", false));
    adapter = new InventoryAdapter(this);
    b.recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    b.recycler.setAdapter(adapter);
    b.lowSwitch.setOnCheckedChangeListener(
        (v, checked) -> {
          prefs.edit().putBoolean("solo_bajo", checked).apply();
          render();
        });
    b.searchEdit.addTextChangedListener(
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int a, int c, int d) {}

          public void onTextChanged(CharSequence s, int a, int before, int count) {
            render();
          }

          public void afterTextChanged(Editable e) {}
        });
    b.addButton.setOnClickListener(v -> startActivity(new Intent(this, ItemFormActivity.class)));
    b.permissionButton.setOnClickListener(v -> askNotifications());
    b.examplesButton.setOnClickListener(v -> model.examples());
    model.repository.items.observe(this, list -> render());
    model.busy.observe(
        this,
        busy -> {
          b.examplesButton.setEnabled(!busy);
          b.addButton.setEnabled(!busy);
        });
    model.result.observe(
        this,
        r -> {
          if (r != null && r.consume()) message(r.message);
        });
  }

  private void render() {
    List<InventoryItem> all = model.repository.items.getValue();
    if (all == null) all = Collections.emptyList();
    String query = b.searchEdit.getText().toString().trim().toLowerCase(Locale.ROOT);
    List<InventoryItem> visible = new ArrayList<>();
    int low = 0;
    for (InventoryItem i : all) {
      if (i.low()) low++;
      if (i.name.toLowerCase(Locale.ROOT).contains(query) && (!b.lowSwitch.isChecked() || i.low()))
        visible.add(i);
    }
    b.summaryText.setText(all.size() + " insumos · " + low + " con stock bajo");
    b.emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    adapter.submit(visible);
  }

  @Override
  public void edit(InventoryItem i) {
    startActivity(new Intent(this, ItemFormActivity.class).putExtra("item_id", i.id));
  }

  @Override
  public void delete(InventoryItem i) {
    new MaterialAlertDialogBuilder(this)
        .setTitle("¿Eliminar insumo?")
        .setMessage(i.name)
        .setNegativeButton("Volver", null)
        .setPositiveButton("Eliminar", (d, w) -> model.delete(i))
        .show();
  }

  @Override
  public void change(InventoryItem i, int delta) {
    model.change(i.id, delta);
  }

  @Override
  public void test(InventoryItem i) {
    message(
        NotificationHelper.show(
                this,
                "test-stock-" + i.id,
                i.low(),
                "Prueba: " + i.name,
                "Disponibles: " + i.quantity + " · Mínimo: " + i.minQuantity,
                new Intent(this, ItemFormActivity.class).putExtra("item_id", i.id))
            ? "Aviso enviado"
            : "Permite las notificaciones y revisa los canales en Ajustes");
  }
}
