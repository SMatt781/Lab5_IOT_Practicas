package com.example.lab5_practica2;

import android.app.Application;
import androidx.lifecycle.*;
import com.example.lab5_practica2.common.OperationResult;

public class InventoryViewModel extends AndroidViewModel {
  public final InventoryRepository repository;
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<OperationResult> result = new MutableLiveData<>();

  public InventoryViewModel(Application app) {
    super(app);
    repository = InventoryRepository.get(app);
  }

  private void done(String error) {
    busy.setValue(false);
    result.setValue(
        new OperationResult(error == null, error == null ? "Operación guardada" : error));
  }

  public void save(InventoryItem i) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.save(i, this::done);
  }

  public void change(long id, int delta) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.change(id, delta, this::done);
  }

  public void delete(InventoryItem i) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.delete(i, this::done);
  }

  public void examples() {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.examples(this::done);
  }
}
