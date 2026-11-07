package com.example.chieuthu3_ketthucmonhoc.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity;

import java.util.List;

@Dao
public interface MenuItemDao {

    @Query("SELECT * FROM menu_items ORDER BY id")
    List<MenuItemEntity> getAll();

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    MenuItemEntity getById(int id);

    @Query("SELECT * FROM menu_items WHERE category = :category ORDER BY id")
    List<MenuItemEntity> getByCategory(String category);

    @Insert
    long insert(MenuItemEntity item);

    @Insert
    void insertAll(List<MenuItemEntity> items);

    @Update
    void update(MenuItemEntity item);

    @Query("DELETE FROM menu_items WHERE id = :id")
    int deleteById(int id);

    @Query("SELECT COUNT(*) FROM menu_items")
    int count();
}
