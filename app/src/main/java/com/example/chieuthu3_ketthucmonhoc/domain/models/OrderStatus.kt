package com.example.chieuthu3_ketthucmonhoc.domain.models

enum class OrderStatus(val displayName: String, val colorHex: String) {
    PENDING("Đang Chờ", "#F59E0B"),       // Amber
    PREPARING("Đang Chế Biến", "#3B82F6"), // Blue
    COMPLETED("Hoàn Thành", "#10B981"),   // Emerald
    CANCELLED("Đã Hủy", "#EF4444");       // Red

    companion object {
        fun fromDisplayName(name: String): OrderStatus {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: PENDING
        }
    }
}
