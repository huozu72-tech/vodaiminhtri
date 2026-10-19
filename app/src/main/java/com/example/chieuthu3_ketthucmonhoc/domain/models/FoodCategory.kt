package com.example.chieuthu3_ketthucmonhoc.domain.models

enum class FoodCategory(val displayName: String) {
    MAIN_DISH("Món chính"),
    SIDE_DISH("Món phụ"),
    BEVERAGE("Nước uống");

    companion object {
        fun fromDisplayName(name: String): FoodCategory {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: MAIN_DISH
        }
    }
}
