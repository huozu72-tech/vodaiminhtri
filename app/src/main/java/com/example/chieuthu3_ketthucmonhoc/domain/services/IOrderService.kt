package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus

interface IOrderService {
    fun createOrder(
        tableNumber: String,
        customerId: Int?,
        cartItems: List<CartItem>,
        paymentMethod: String = "Chuyển khoản QR"
    ): Order

    fun generatePaymentQrUrl(order: Order): String
    fun getOrderById(orderId: String): Order?
    fun getCustomerOrders(customerId: Int): List<Order>
    fun getAllOrders(): List<Order>
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean
    fun cancelOrder(orderId: String): Boolean
}
