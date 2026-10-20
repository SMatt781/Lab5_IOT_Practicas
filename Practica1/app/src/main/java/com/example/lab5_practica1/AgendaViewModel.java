package com.example.lab5_practica1;

import android.app.Application;
import androidx.lifecycle.*;
import com.example.lab5_practica1.common.OperationResult;

public class AgendaViewModel extends AndroidViewModel {
  public final EventRepository repository;
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<OperationResult> result = new MutableLiveData<>();

  public AgendaViewModel(Application app) {
    super(app);
    repository = EventRepository.get(app);
  }

  public void save(Event e) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.save(
        e,
        error -> {
          busy.setValue(false);
          result.setValue(
              new OperationResult(error == null, error == null ? "Evento guardado" : error));
        });
  }

  public void delete(Event e) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.delete(
        e,
        error -> {
          busy.setValue(false);
          result.setValue(
              new OperationResult(error == null, error == null ? "Evento eliminado" : error));
        });
  }
}
