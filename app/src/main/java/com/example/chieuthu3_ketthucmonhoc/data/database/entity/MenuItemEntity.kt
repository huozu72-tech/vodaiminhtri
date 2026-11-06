package com.example.chieuthu3_ketthucmonhoc.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute

/**
 * Bảng thực đơn. Ràng buộc: tên không rỗng (NOT NULL), giá trị enums hợp lệ do mapper đảm bảo;
 * ràng buộc giá > 0 nằm ở tầng service (MenuServiceImpl) theo đúng phân lớp hiện có.
 * Bị order_items tham chiếu bằng FK RESTRICT nên món đã nằm trong đơn không bị xóa cứng.
 */
@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Long,
    val category: String,
    val tasteAttributes: String,
    val isAvailable: Boolean,
    val rating: Double,
    val orderCount: Int,
    @ColumnInfo(name = "image_res_id") val imageResId: Int
)

fun MenuItemEntity.toDomain(): MenuItem = MenuItem(
    id = id,
    name = name,
    description = description,
    price = price,
    category = FoodCategory.valueOf(category),
    tasteAttributes = tasteAttributes.split(',')
        .filter { it.isNotBlank() }
        .map { TasteAttribute.valueOf(it.trim()) },
    isAvailable = isAvailable,
    rating = rating,
    orderCount = orderCount,
    imageResId = imageResId
)

fun MenuItem.toEntity(): MenuItemEntity = MenuItemEntity(
    id = id,
    name = name.trim(),
    description = description,
    price = price,
    category = category.name,
    tasteAttributes = tasteAttributes.joinToString(",") { it.name },
    isAvailable = isAvailable,
    rating = rating,
    orderCount = orderCount,
    imageResId = imageResId
)
