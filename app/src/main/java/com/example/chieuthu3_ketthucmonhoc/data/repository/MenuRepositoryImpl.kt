package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import java.text.Normalizer
import java.util.concurrent.CopyOnWriteArrayList

class MenuRepositoryImpl(
    initialItems: List<MenuItem> = InitialDataFactory.createInitialMenuItems()
) : IMenuRepository {

    private val menuList = CopyOnWriteArrayList<MenuItem>(initialItems)
    private var nextId = (menuList.maxOfOrNull { it.id } ?: 0) + 1

    override fun getAllMenuItems(): List<MenuItem> = menuList.toList()

    override fun getMenuItemById(id: Int): MenuItem? {
        return menuList.find { it.id == id }
    }

    override fun getMenuItemsByCategory(category: FoodCategory): List<MenuItem> {
        return menuList.filter { it.category == category }
    }

    override fun saveMenuItem(item: MenuItem): MenuItem {
        val target = if (item.id <= 0) {
            item.copy(id = nextId++)
        } else {
            item
        }
        val idx = menuList.indexOfFirst { it.id == target.id }
        if (idx >= 0) {
            menuList[idx] = target
        } else {
            menuList.add(target)
        }
        return target
    }

    override fun deleteMenuItem(id: Int): Boolean {
        return menuList.removeIf { it.id == id }
    }

    override fun searchMenuItems(query: String): List<MenuItem> {
        if (query.isBlank()) return getAllMenuItems()
        val normalizedQuery = removeDiacritics(query.trim().lowercase())
        return menuList.filter {
            val normalizedName = removeDiacritics(it.name.lowercase())
            val normalizedDesc = removeDiacritics(it.description.lowercase())
            val normalizedCat = removeDiacritics(it.category.displayName.lowercase())
            normalizedName.contains(normalizedQuery) ||
                    normalizedDesc.contains(normalizedQuery) ||
                    normalizedCat.contains(normalizedQuery)
        }
    }

    private fun removeDiacritics(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return regex.replace(normalized, "").replace("đ", "d").replace("Đ", "D")
    }
}
