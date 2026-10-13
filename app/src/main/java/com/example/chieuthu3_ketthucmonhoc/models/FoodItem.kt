package com.example.chieuthu3_ketthucmonhoc.models

data class FoodItem(
    val id: Int,
    val name: String,
    val category: String, // "Món chính", "Món phụ", "Nước uống"
    val price: Int,
    val priceFormatted: String,
    val description: String
)
