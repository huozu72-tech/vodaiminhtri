package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import java.net.URLEncoder
import java.util.Random

class OrderServiceImpl(
    private val orderRepository: IOrderRepository
) : IOrderService {

    private val random = Random()

    override fun createOrder(
        tableNumber: String,
        customerId: Int?,
        cartItems: List<CartItem>,
        paymentMethod: String
    ): Order {
        require(tableNumber.isNotBlank()) { "Số bàn không được để trống" }
        require(cartItems.isNotEmpty()) { "Giỏ hàng không được rỗng khi tạo đơn" }

        val orderCode = "#ORD-${1000 + random.nextInt(9000)}"
        val orderItems = cartItems.mapIndexed { index, cartItem ->
            OrderItem(
                id = index + 1,
                menuItem = cartItem.menuItem,
                quantity = cartItem.quantity,
                unitPrice = cartItem.menuItem.price
            )
        }

        val totalAmount = orderItems.sumOf { it.subtotal }
        val newOrder = Order(
            orderId = orderCode,
            tableNumber = tableNumber.trim(),
            customerId = customerId,
            items = orderItems,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            qrPayloadUrl = generatePaymentQrUrlInternal(orderCode, totalAmount)
        )

        return orderRepository.saveOrder(newOrder)
    }

    override fun generatePaymentQrUrl(order: Order): String {
        return generatePaymentQrUrlInternal(order.orderId, order.totalAmount)
    }

    private fun generatePaymentQrUrlInternal(orderId: String, amount: Long): String {
        val bankId = "MBBank"
        val accountNo = "0901234567"
        val template = "compact2"
        val description = URLEncoder.encode("Thanh toan $orderId", "UTF-8")
        return "https://img.vietqr.io/image/$bankId-$accountNo-$template.png?amount=$amount&addInfo=$description"
    }

    override fun getOrderById(orderId: String): Order? {
        return orderRepository.getOrderById(orderId)
    }

    override fun getCustomerOrders(customerId: Int): List<Order> {
        return orderRepository.getOrdersByCustomer(customerId)
    }

    override fun getAllOrders(): List<Order> {
        return orderRepository.getAllOrders()
    }

    override fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        return orderRepository.updateOrderStatus(orderId, newStatus)
    }

    override fun cancelOrder(orderId: String): Boolean {
        val order = orderRepository.getOrderById(orderId) ?: return false
        if (order.status == OrderStatus.COMPLETED) {
            return false // Cannot cancel completed orders
        }
        return orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }
}
