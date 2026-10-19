package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DashboardServiceTest {

    private lateinit var dashboardService: DashboardServiceImpl
    private lateinit var menuRepository: MenuRepositoryImpl
    private lateinit var orderRepository: OrderRepositoryImpl

    private val dish1 = MenuItem(1, "Chân Giò Muối Chiên Giòn", "", 120000, FoodCategory.MAIN_DISH)
    private val dish2 = MenuItem(2, "Coca Cola", "", 15000, FoodCategory.BEVERAGE)

    @Before
    fun setup() {
        menuRepository = MenuRepositoryImpl(listOf(dish1, dish2))
        val orders = listOf(
            Order("#ORD-1", "Bàn 1", 1, listOf(OrderItem(1, dish1, 1), OrderItem(2, dish2, 2)), 150000, OrderStatus.COMPLETED),
            Order("#ORD-2", "Bàn 2", 1, listOf(OrderItem(3, dish1, 1)), 120000, OrderStatus.PENDING),
            Order("#ORD-3", "Bàn 3", 1, listOf(OrderItem(4, dish2, 1)), 15000, OrderStatus.CANCELLED)
        )
        orderRepository = OrderRepositoryImpl(listOf(dish1, dish2), orders)
        dashboardService = DashboardServiceImpl(menuRepository, orderRepository)
    }

    @Test
    fun `dashboard metrics calculates revenue and completion rate correctly`() {
        val metrics = dashboardService.getDashboardMetrics()
        // Only completed orders contribute to total revenue
        assertEquals(150000L, metrics.totalRevenue)
        assertEquals(3, metrics.totalOrdersCount)
        assertEquals(1, metrics.completedOrdersCount)
        assertEquals(1, metrics.pendingOrdersCount)
        assertEquals(2, metrics.totalMenuItemsCount)
        assertEquals(33, metrics.completionRatePercent) // 1 out of 3 = 33%
        assertTrue(metrics.formattedRevenue.contains("150.000"))
    }

    @Test
    fun `top selling items sorts by total quantity ordered`() {
        val top = dashboardService.getTopSellingMenuItems(2)
        assertEquals(2, top.size)
        // dish2 was ordered: 2 (in order 1) + 1 (in order 3) = 3
        // dish1 was ordered: 1 (in order 1) + 1 (in order 2) = 2
        assertEquals(dish2.id, top[0].first.id)
        assertEquals(3, top[0].second)
        assertEquals(dish1.id, top[1].first.id)
        assertEquals(2, top[1].second)
    }
}
