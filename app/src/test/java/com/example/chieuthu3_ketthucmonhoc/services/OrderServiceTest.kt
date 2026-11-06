package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem
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

    @Test
    fun `order codes never duplicate existing or previously created orders`() {
        val seededRepository = OrderRepositoryImpl(
            menuItems = listOf(food1, food2),
            initialOrders = listOf(
                Order(orderId = "#ORD-1023", tableNumber = "Bàn 1", customerId = 1, items = listOf(OrderItem(1, food1, 1))),
                Order(orderId = "#ORD-1025", tableNumber = "Bàn 2", customerId = 2, items = listOf(OrderItem(1, food2, 2)))
            )
        )
        val seededService = OrderServiceImpl(seededRepository)

        // Mã mới phải lớn hơn mã seed lớn nhất thay vì ngẫu nhiên có nguy cơ trùng
        val first = seededService.createOrder("Bàn 05", 1, listOf(CartItem(food1, 1)))
        assertEquals("#ORD-1026", first.orderId)

        val second = seededService.createOrder("Bàn 06", 1, listOf(CartItem(food2, 1)))
        val third = seededService.createOrder("Bàn 07", 1, listOf(CartItem(food2, 1)))
        assertTrue(second.orderId != third.orderId)

        // Không đơn nào bị ghi đè: đủ 2 đơn seed + 3 đơn mới
        assertEquals(5, seededService.getAllOrders().size)
    }

    @Test
    fun `order status cannot jump from pending straight to completed`() {
        val order = orderService.createOrder("Bàn 02", 1, listOf(CartItem(food1, 1)))

        val jumped = orderService.updateOrderStatus(order.orderId, OrderStatus.COMPLETED)
        assertFalse(jumped)
        assertEquals(OrderStatus.PENDING, orderService.getOrderById(order.orderId)?.status)
    }

    @Test
    fun `completed order status is final and cannot go backwards`() {
        val order = orderService.createOrder("Bàn 03", 1, listOf(CartItem(food1, 1)))
        assertTrue(orderService.updateOrderStatus(order.orderId, OrderStatus.PREPARING))
        assertTrue(orderService.updateOrderStatus(order.orderId, OrderStatus.COMPLETED))

        assertFalse(orderService.updateOrderStatus(order.orderId, OrderStatus.PENDING))
        assertFalse(orderService.updateOrderStatus(order.orderId, OrderStatus.PREPARING))
        assertFalse(orderService.updateOrderStatus(order.orderId, OrderStatus.CANCELLED))
        assertEquals(OrderStatus.COMPLETED, orderService.getOrderById(order.orderId)?.status)
    }

    @Test
    fun `cancelled order cannot be cancelled again or revived`() {
        val order = orderService.createOrder("Bàn 04", 1, listOf(CartItem(food1, 1)))

        assertTrue(orderService.cancelOrder(order.orderId))
        assertEquals(OrderStatus.CANCELLED, orderService.getOrderById(order.orderId)?.status)

        assertFalse(orderService.cancelOrder(order.orderId))
        assertFalse(orderService.updateOrderStatus(order.orderId, OrderStatus.PREPARING))
        assertFalse(orderService.updateOrderStatus(order.orderId, OrderStatus.PENDING))
        assertEquals(OrderStatus.CANCELLED, orderService.getOrderById(order.orderId)?.status)
    }

    @Test
    fun `update status of unknown order returns false`() {
        assertFalse(orderService.updateOrderStatus("#ORD-KHONG-TON-TAI", OrderStatus.PREPARING))
    }

    @Test
    fun `repository saveOrder with different case id updates instead of duplicating`() {
        val original = Order(
            orderId = "#ORD-2000",
            tableNumber = "Bàn 1",
            customerId = 1,
            items = listOf(OrderItem(1, food1, 1)),
            status = OrderStatus.PENDING
        )
        orderRepository.saveOrder(original)
        orderRepository.saveOrder(original.copy(orderId = "#ord-2000", status = OrderStatus.PREPARING))

        assertEquals(1, orderRepository.getAllOrders().size)
        assertEquals(OrderStatus.PREPARING, orderRepository.getOrderById("#ORD-2000")?.status)
    }
}
