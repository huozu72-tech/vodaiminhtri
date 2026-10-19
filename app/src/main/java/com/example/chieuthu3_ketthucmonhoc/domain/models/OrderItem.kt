package com.example.chieuthu3_ketthucmonhoc.domain.models

import java.text.NumberFormat
import java.util.Locale

data class OrderItem(
    val id: Int,
    val menuItem: MenuItem,
    val quantity: Int,
    val unitPrice: Long = menuItem.price
) {
    val subtotal: Long
        get() = unitPrice * quantity

    val formattedSubtotal: String
        get() {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${formatter.format(subtotal)} đ"
        }
}
