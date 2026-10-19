package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute

class MenuServiceImpl(
    private val menuRepository: IMenuRepository
) : IMenuService {

    override fun getAllMenuItems(): List<MenuItem> {
        return menuRepository.getAllMenuItems()
    }

    override fun getAvailableMenuItems(): List<MenuItem> {
        return menuRepository.getAllMenuItems().filter { it.isAvailable }
    }

    override fun getMenuItemsByCategory(category: FoodCategory): List<MenuItem> {
        return menuRepository.getMenuItemsByCategory(category).filter { it.isAvailable }
    }

    override fun searchMenuItems(query: String): List<MenuItem> {
        return menuRepository.searchMenuItems(query)
    }

    override fun getMenuItemById(id: Int): MenuItem? {
        return menuRepository.getMenuItemById(id)
    }

    override fun createMenuItem(
        name: String,
        description: String,
        price: Long,
        category: FoodCategory,
        tasteAttributes: List<TasteAttribute>
    ): MenuItem {
        require(name.isNotBlank()) { "Tên món ăn không được để trống" }
        require(price > 0) { "Giá món ăn phải lớn hơn 0" }

        val newItem = MenuItem(
            id = 0,
            name = name.trim(),
            description = description.trim(),
            price = price,
            category = category,
            tasteAttributes = tasteAttributes,
            isAvailable = true
        )
        return menuRepository.saveMenuItem(newItem)
    }

    override fun updateMenuItem(item: MenuItem): MenuItem {
        require(item.name.isNotBlank()) { "Tên món ăn không được để trống" }
        require(item.price > 0) { "Giá món ăn phải lớn hơn 0" }
        return menuRepository.saveMenuItem(item)
    }

    override fun deleteMenuItem(id: Int): Boolean {
        return menuRepository.deleteMenuItem(id)
    }

    override fun toggleAvailability(id: Int): MenuItem? {
        val item = menuRepository.getMenuItemById(id) ?: return null
        val updated = item.copy(isAvailable = !item.isAvailable)
        return menuRepository.saveMenuItem(updated)
    }
}
