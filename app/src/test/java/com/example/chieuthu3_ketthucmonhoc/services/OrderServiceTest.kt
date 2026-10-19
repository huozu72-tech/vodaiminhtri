package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import com.example.chieuthu3_ketthucmonhoc.domain.services.OrderServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OrderServiceTest {

    private lateinit var orderService: OrderServiceImpl
    private lateinit var orderRepository: OrderRepositoryImpl

    private val food1 = MenuItem(1, "Chân Giò Muối Chiên Giòn", "Thơm ngon", 120000, FoodCategory.MAIN_DISH)
    private val food2 = MenuItem(2, "7Up", "Nước ngọt", 15000, FoodCategory.BEVERAGE)

    @Before
    fun setup() {
        orderRepository = OrderRepositoryImpl(
            menuItems = listOf(food1, food2),
            initialOrders = emptyList()
        )
        orderService = OrderServiceImpl(orderRepository)
    }

    @Test
    fun `create order calculates total and generates QR url`() {
        val cartItems = listOf(
            CartItem(food1, 1),
            CartItem(food2, 2)
        )

        val order = orderService.createOrder("Bàn 08", 1, cartItems)
        assertNotNull(order.orderId)
        assertTrue(order.orderId.startsWith("#ORD-"))
        assertEquals("Bàn 08", order.tableNumber)
        assertEquals(150000L, order.totalAmount)
        assertEquals(OrderStatus.PENDING, order.status)
        assertNotNull(order.qrPayloadUrl)
        assertTrue(order.qrPayloadUrl!!.contains("vietqr.io"))
        assertTrue(order.qrPayloadUrl!!.contains("amount=150000"))
    }

    @Test
    fun `order status updates correctly`() {
        val cartItems = listOf(CartItem(food1, 1))
        val order = orderService.createOrder("Bàn 01", 1, cartItems)

        val updated = orderService.updateOrderStatus(order.orderId, OrderStatus.PREPARING)
        assertTrue(updated)
        assertEquals(OrderStatus.PREPARING, orderService.getOrderById(order.orderId)?.status)

        orderService.updateOrderStatus(order.orderId, OrderStatus.COMPLETED)
        assertEquals(OrderStatus.COMPLETED, orderService.getOrderById(order.orderId)?.status)

        // Cannot cancel completed order
        val cancelled = orderService.cancelOrder(order.orderId)
        assertFalse(cancelled)
        assertEquals(OrderStatus.COMPLETED, orderService.getOrderById(order.orderId)?.status)
    }
}
