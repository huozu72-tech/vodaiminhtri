package com.example.chieuthu3_ketthucmonhoc.domain.models

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Order(
    val orderId: String,
    val tableNumber: String,
    val customerId: Int? = null,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Long = items.sumOf { it.subtotal },
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: String = "Chuyển khoản QR",
    val qrPayloadUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val itemsSummary: String
        get() = items.joinToString(", ") { "${it.menuItem.name} x${it.quantity}" }

    val formattedTotal: String
        get() {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${formatter.format(totalAmount)} đ"
        }

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale("vi", "VN"))
            return sdf.format(Date(createdAt))
        }
}
