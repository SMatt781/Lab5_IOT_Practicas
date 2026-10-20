package com.example.lab5_practica2;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface InventoryDao {
  @Query("SELECT * FROM insumos ORDER BY name COLLATE NOCASE")
  LiveData<List<InventoryItem>> observeAll();

  @Query("SELECT * FROM insumos WHERE id=:id")
  InventoryItem find(long id);

  @Query("SELECT COUNT(*) FROM insumos")
  int count();

  @Insert
  long insert(InventoryItem item);

  @Update
  int update(InventoryItem item);

  @Delete
  int delete(InventoryItem item);
}
