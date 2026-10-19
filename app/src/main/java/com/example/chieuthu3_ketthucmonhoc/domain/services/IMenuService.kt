package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute

interface IMenuService {
    fun getAllMenuItems(): List<MenuItem>
    fun getAvailableMenuItems(): List<MenuItem>
    fun getMenuItemsByCategory(category: FoodCategory): List<MenuItem>
    fun searchMenuItems(query: String): List<MenuItem>
    fun getMenuItemById(id: Int): MenuItem?
    fun createMenuItem(
        name: String,
        description: String,
        price: Long,
        category: FoodCategory,
        tasteAttributes: List<TasteAttribute> = emptyList()
    ): MenuItem
    fun updateMenuItem(item: MenuItem): MenuItem
    fun deleteMenuItem(id: Int): Boolean
    fun toggleAvailability(id: Int): MenuItem?
}
