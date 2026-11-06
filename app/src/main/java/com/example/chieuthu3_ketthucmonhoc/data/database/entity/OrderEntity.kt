package com.example.chieuthu3_ketthucmonhoc.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus

/**
 * Bảng đơn hàng. Ràng buộc:
 * - customer_id FK -> customers(id) ON DELETE SET NULL (xóa khách không làm mất lịch sử đơn);
 * - mã đơn là khóa chính (UNIQUE), trạng thái lưu tên enum hợp lệ do mapper đảm bảo.
 */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("customer_id")]
)
data class OrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val orderId: String,
    val tableNumber: String,
    @ColumnInfo(name = "customer_id") val customerId: Int?,
    val totalAmount: Long,
    val status: String,
    val paymentMethod: String,
    val qrPayloadUrl: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

fun OrderEntity.toDomain(): Order = Order(
    orderId = orderId,
    tableNumber = tableNumber,
    customerId = customerId,
    totalAmount = totalAmount,
    status = OrderStatus.valueOf(status),
    paymentMethod = paymentMethod,
    qrPayloadUrl = qrPayloadUrl,
    createdAt = createdAt
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    orderId = orderId.trim(),
    tableNumber = tableNumber,
    customerId = customerId,
    totalAmount = totalAmount,
    status = status.name,
    paymentMethod = paymentMethod,
    qrPayloadUrl = qrPayloadUrl,
    createdAt = createdAt
)
