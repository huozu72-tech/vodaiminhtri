package com.example.chieuthu3_ketthucmonhoc.domain.models

import java.text.NumberFormat
import java.util.Locale

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int
) {
    val subtotal: Long
        get() = menuItem.price * quantity

    val formattedSubtotal: String
        get() {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${formatter.format(subtotal)} đ"
        }
}
