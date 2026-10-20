package com.example.lab5_practica3;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExpenseStore {
  private final AtomicFile file;

  public ExpenseStore(Context c) {
    File dir = new File(c.getFilesDir(), "finanzas");
    if (!dir.exists() && !dir.mkdirs())
      throw new IllegalStateException("No se pudo crear la carpeta");
    file = new AtomicFile(new File(dir, "gastos.json"));
  }

  public List<Expense> read() throws IOException {
    try (FileInputStream input = file.openRead()) {
      return LedgerCodec.decode(readText(input));
    } catch (FileNotFoundException empty) {
      return new ArrayList<>();
    }
  }

  public void write(List<Expense> expenses) throws IOException {
    byte[] data = LedgerCodec.encode(expenses).getBytes(StandardCharsets.UTF_8);
    if (data.length > 2 * 1024 * 1024)
      throw new IOException("El archivo supera el límite didáctico de 2 MB");
    FileOutputStream output = null;
    try {
      output = file.startWrite();
      output.write(data);
      file.finishWrite(output);
    } catch (IOException ex) {
      if (output != null) file.failWrite(output);
      throw ex;
    }
  }

  public static String readText(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int count, total = 0;
    while ((count = in.read(buffer)) != -1) {
      total += count;
      if (total > 2 * 1024 * 1024) throw new IOException("Archivo demasiado grande (máximo 2 MB)");
      out.write(buffer, 0, count);
    }
    return out.toString(StandardCharsets.UTF_8.name());
  }
}
