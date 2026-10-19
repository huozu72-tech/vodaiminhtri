package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

interface IMenuRepository {
    fun getAllMenuItems(): List<MenuItem>
    fun getMenuItemById(id: Int): MenuItem?
    fun getMenuItemsByCategory(category: FoodCategory): List<MenuItem>
    fun saveMenuItem(item: MenuItem): MenuItem
    fun deleteMenuItem(id: Int): Boolean
    fun searchMenuItems(query: String): List<MenuItem>
}
