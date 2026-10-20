package com.example.lab5_practica1;

import android.view.*;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab5_practica1.databinding.RowEventBinding;
import java.time.*;
import java.util.*;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.Holder> {
  public interface Actions {
    void edit(Event e);

    void delete(Event e);

    void test(Event e);
  }

  private List<Event> items = new ArrayList<>();
  private final Actions actions;
  private LocalDateTime from = LocalDateTime.now();

  public EventAdapter(Actions a) {
    actions = a;
  }

  public void submit(List<Event> list, LocalDateTime start) {
    from = start;
    items = new ArrayList<>(list);
    items.sort(Comparator.comparing(e -> EventDates.next(e, from)));
    notifyDataSetChanged();
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup p, int type) {
    return new Holder(RowEventBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  @Override
  public void onBindViewHolder(Holder h, int pos) {
    Event e = items.get(pos);
    h.b.nameText.setText(e.name);
    h.b.periodText.setText((e.annual ? "ANUAL" : "UNA VEZ") + " · " + e.leadDays + " días antes");
    h.b.periodText.setTextColor(
        h.itemView.getContext().getColor(e.annual ? R.color.lab_high : R.color.lab_normal));
    h.b.dateText.setText(EventDates.label(e, EventDates.next(e, from)));
    h.b.remainingText.setText(
        EventDates.remaining(EventDates.next(e, from), LocalDateTime.now()));
    h.b.editButton.setOnClickListener(v -> actions.edit(e));
    h.b.deleteButton.setOnClickListener(v -> actions.delete(e));
    h.b.testButton.setOnClickListener(v -> actions.test(e));
  }

  static class Holder extends RecyclerView.ViewHolder {
    final RowEventBinding b;

    Holder(RowEventBinding b) {
      super(b.getRoot());
      this.b = b;
    }
  }
}
