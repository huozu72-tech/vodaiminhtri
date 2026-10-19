package com.example.chieuthu3_ketthucmonhoc.domain.models

enum class TasteAttribute(val tag: String) {
    SPICY("cay"),
    SWEET("ngọt"),
    CRISPY("giòn"),
    SAVORY("đậm đà"),
    SOUR("chua"),
    REFRESHING("thanh mát"),
    GRILLED("nướng"),
    FRIED("chiên");

    companion object {
        fun parseFromText(text: String): List<TasteAttribute> {
            val lower = text.lowercase()
            return entries.filter { lower.contains(it.tag) }
        }
    }
}
