package com.example.chieuthu3_ketthucmonhoc.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem

/**
 * Bảng món trong đơn. Ràng buộc:
 * - PK đồn (order_id, id): id món thứ tự là cục bộ theo từng đơn như domain model;
 * - order_id FK -> orders(orderId) ON DELETE CASCADE: xóa đơn là sạch cả chi tiết;
 * - menu_item_id FK -> menu_items(id) ON DELETE RESTRICT: không xóa món đã nằm trong đơn
 *   (bảo vệ lịch sử giá/đơn);
 * - UNIQUE(order_id, menu_item_id): một món chỉ xuất hiện một dòng trong mỗi đơn
 *   (đúng cách giỏ hàng gom số lượng theo món).
 */
@Entity(
    tableName = "order_items",
    primaryKeys = ["order_id", "id"],
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["order_id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("order_id"),
        Index("menu_item_id"),
        Index(value = ["order_id", "menu_item_id"], unique = true)
    ]
)
data class OrderItemEntity(
    val id: Int,
    @ColumnInfo(name = "order_id") val orderId: String,
    @ColumnInfo(name = "menu_item_id") val menuItemId: Int,
    val quantity: Int,
    val unitPrice: Long
)

fun OrderItem.toEntity(orderId: String): OrderItemEntity = OrderItemEntity(
    id = id,
    orderId = orderId,
    menuItemId = menuItem.id,
    quantity = quantity,
    unitPrice = unitPrice
)

/** Lắp lại OrderItem domain: đơn giá lịch sử lấy từ cột unit_price, thông tin món đọc hiện thời từ thực đơn. */
fun OrderItemEntity.toDomain(menuItem: MenuItem): OrderItem = OrderItem(
    id = id,
    menuItem = menuItem,
    quantity = quantity,
    unitPrice = unitPrice
)
