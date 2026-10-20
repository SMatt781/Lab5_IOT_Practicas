package com.example.lab5_practica3;

import com.google.gson.*;
import java.util.*;

public final class LedgerCodec {
  public static class Document {
    public int version = 1;
    public List<Expense> expenses = new ArrayList<>();
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private LedgerCodec() {}

  public static List<Expense> decode(String json) {
    JsonElement raw = JsonParser.parseString(json);
    if (!raw.isJsonObject()
        || !raw.getAsJsonObject().has("version")
        || !raw.getAsJsonObject().has("expenses"))
      throw new IllegalArgumentException("No es un archivo de Mi presupuesto");
    Document doc = GSON.fromJson(raw, Document.class);
    if (doc == null || doc.version != 1 || doc.expenses == null || doc.expenses.size() > 10000)
      throw new IllegalArgumentException("Versión o tamaño no admitido");
    Set<String> ids = new HashSet<>();
    for (Expense e : doc.expenses) {
      String error = MoneyRules.validate(e);
      if (error != null) throw new IllegalArgumentException(error);
      if (!ids.add(e.id))
        throw new IllegalArgumentException("El archivo tiene identificadores repetidos");
    }
    return doc.expenses;
  }

  public static String encode(List<Expense> list) {
    Document doc = new Document();
    doc.expenses = new ArrayList<>(list);
    return GSON.toJson(doc);
  }

  public static List<Expense> merge(List<Expense> existing, List<Expense> incoming) {
    Map<String, Expense> map = new LinkedHashMap<>();
    for (Expense e : existing) map.put(e.id, e);
    for (Expense e : incoming) map.putIfAbsent(e.id, e);
    if (map.size() > 10000) throw new IllegalArgumentException("Se permiten hasta 10000 gastos");
    return new ArrayList<>(map.values());
  }
}
