package com.example.chieuthu3_ketthucmonhoc.models

data class Order(
    val id: String,
    val tableName: String,
    val timeString: String,
    val itemsSummary: String,
    val totalPriceFormatted: String,
    var status: String // "Đang Chờ", "Đang Chế Biến", "Hoàn Thành", "Đã Hủy"
)
