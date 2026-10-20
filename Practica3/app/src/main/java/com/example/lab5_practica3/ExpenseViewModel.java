package com.example.lab5_practica3;

import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.*;
import com.example.lab5_practica3.common.OperationResult;

public class ExpenseViewModel extends AndroidViewModel {
  public final ExpenseRepository repository;
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<OperationResult> result = new MutableLiveData<>();

  public ExpenseViewModel(Application app) {
    super(app);
    repository = ExpenseRepository.get(app);
  }

  private void done(String error) {
    busy.setValue(false);
    result.setValue(
        new OperationResult(error == null, error == null ? "Operación completada" : error));
  }

  private boolean start() {
    if (Boolean.TRUE.equals(busy.getValue())) return false;
    busy.setValue(true);
    return true;
  }

  public void save(Expense e) {
    if (start()) repository.save(e, this::done);
  }

  public void delete(Expense e) {
    if (start()) repository.delete(e, this::done);
  }

  public void budget(String month, long cents) {
    if (start()) repository.setBudget(month, cents, this::done);
  }

  public void exportFile(Uri uri) {
    if (start()) repository.exportFile(uri, this::done);
  }

  public void importFile(Uri uri) {
    if (start()) repository.importFile(uri, this::done);
  }
}
