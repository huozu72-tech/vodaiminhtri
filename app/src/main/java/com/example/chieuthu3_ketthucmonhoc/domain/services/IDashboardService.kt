package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import java.text.NumberFormat
import java.util.Locale

data class DashboardMetrics(
    val totalRevenue: Long,
    val totalOrdersCount: Int,
    val completedOrdersCount: Int,
    val pendingOrdersCount: Int,
    val totalMenuItemsCount: Int,
    val completionRatePercent: Int
) {
    val formattedRevenue: String
        get() {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return "${formatter.format(totalRevenue)} đ"
        }
}

interface IDashboardService {
    fun getDashboardMetrics(): DashboardMetrics
    fun getTopSellingMenuItems(limit: Int = 5): List<Pair<MenuItem, Int>>
}
