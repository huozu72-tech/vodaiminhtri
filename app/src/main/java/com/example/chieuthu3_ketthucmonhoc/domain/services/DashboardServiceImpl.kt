package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus

class DashboardServiceImpl(
    private val menuRepository: IMenuRepository,
    private val orderRepository: IOrderRepository
) : IDashboardService {

    override fun getDashboardMetrics(): DashboardMetrics {
        val allOrders = orderRepository.getAllOrders()
        val allMenuItems = menuRepository.getAllMenuItems()

        val completedOrders = allOrders.filter { it.status == OrderStatus.COMPLETED }
        val pendingOrders = allOrders.filter { it.status == OrderStatus.PENDING || it.status == OrderStatus.PREPARING }

        val totalRevenue = completedOrders.sumOf { it.totalAmount }
        val totalCount = allOrders.size
        val completedCount = completedOrders.size
        val completionRate = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        return DashboardMetrics(
            totalRevenue = totalRevenue,
            totalOrdersCount = totalCount,
            completedOrdersCount = completedCount,
            pendingOrdersCount = pendingOrders.size,
            totalMenuItemsCount = allMenuItems.size,
            completionRatePercent = completionRate
        )
    }

    override fun getTopSellingMenuItems(limit: Int): List<Pair<MenuItem, Int>> {
        val allOrders = orderRepository.getAllOrders()
        val itemCounts = mutableMapOf<Int, Int>()

        for (order in allOrders) {
            for (orderItem in order.items) {
                val current = itemCounts[orderItem.menuItem.id] ?: 0
                itemCounts[orderItem.menuItem.id] = current + orderItem.quantity
            }
        }

        return itemCounts.entries
            .mapNotNull { entry ->
                val menuItem = menuRepository.getMenuItemById(entry.key)
                if (menuItem != null) Pair(menuItem, entry.value) else null
            }
            .sortedByDescending { it.second }
            .take(limit)
    }
}
