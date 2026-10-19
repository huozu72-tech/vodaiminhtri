package com.example.chieuthu3_ketthucmonhoc.domain.models

import java.text.NumberFormat
import java.util.Locale

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Long,
    val category: FoodCategory,
    val tasteAttributes: List<TasteAttribute> = emptyList(),
    val isAvailable: Boolean = true,
    val rating: Double = 4.8,
    val orderCount: Int = 0,
    val imageResId: Int = 0
) {
    val formattedPrice: String
        get() {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${formatter.format(price)} đ"
        }
}
