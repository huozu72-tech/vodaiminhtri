package com.example.chieuthu3_ketthucmonhoc.data.database

import android.database.sqlite.SQLiteConstraintException
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toDomain
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toEntity
import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import java.text.Normalizer

/**
 * Repository thực đơn chạy trên SQLite (Room), thay thế bản in-memory ở tầng wiring.
 * FK RESTRICT từ order_items khiến món đã nằm trong đơn không xóa cứng được:
 * khi bị chặn, fallback thành ẩn món (isAvailable = false) để giữ nguyên lịch sử đơn.
 */
class RoomMenuRepository(
    private val database: AppDatabase
) : IMenuRepository {

    private val dao = database.menuItemDao()

    override fun getAllMenuItems(): List<MenuItem> = dao.getAll().map { it.toDomain() }

    override fun getMenuItemById(id: Int): MenuItem? = dao.getById(id)?.toDomain()

    override fun getMenuItemsByCategory(category: FoodCategory): List<MenuItem> =
        dao.getByCategory(category.name).map { it.toDomain() }

    override fun saveMenuItem(item: MenuItem): MenuItem {
        val entity = item.toEntity()
        if (entity.id <= 0) {
            val newId = dao.insert(entity.copy(id = 0))
            return item.copy(id = newId.toInt())
        }
        val existing = dao.getById(entity.id)
        return if (existing == null) {
            dao.insert(entity)
            item
        } else {
            dao.update(entity)
            item
        }
    }

    override fun deleteMenuItem(id: Int): Boolean {
        val existing = dao.getById(id) ?: return false
        return try {
            dao.deleteById(id) > 0
        } catch (_: SQLiteConstraintException) {
            // Món đang được đơn hàng tham chiếu (FK RESTRICT): ẩn món thay vì xóa cứng
            dao.update(existing.copy(isAvailable = false))
            true
        }
    }

    override fun searchMenuItems(query: String): List<MenuItem> {
        if (query.isBlank()) return getAllMenuItems()
        val normalizedQuery = removeDiacritics(query.trim().lowercase())
        return dao.getAll().map { it.toDomain() }.filter { item ->
            val normalizedName = removeDiacritics(item.name.lowercase())
            val normalizedDesc = removeDiacritics(item.description.lowercase())
            val normalizedCat = removeDiacritics(item.category.displayName.lowercase())
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
