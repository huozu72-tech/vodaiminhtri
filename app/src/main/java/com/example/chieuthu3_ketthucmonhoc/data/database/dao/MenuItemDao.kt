package com.example.chieuthu3_ketthucmonhoc.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity

@Dao
interface MenuItemDao {

    @Query("SELECT * FROM menu_items ORDER BY id")
    fun getAll(): List<MenuItemEntity>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    fun getById(id: Int): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE category = :category ORDER BY id")
    fun getByCategory(category: String): List<MenuItemEntity>

    @Insert
    fun insert(item: MenuItemEntity): Long

    @Insert
    fun insertAll(items: List<MenuItemEntity>)

    @Update
    fun update(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    fun deleteById(id: Int): Int

    @Query("SELECT COUNT(*) FROM menu_items")
    fun count(): Int
}
