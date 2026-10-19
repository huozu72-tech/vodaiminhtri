package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus

interface IOrderRepository {
    fun getAllOrders(): List<Order>
    fun getOrderById(orderId: String): Order?
    fun getOrdersByCustomer(customerId: Int): List<Order>
    fun saveOrder(order: Order): Order
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean
}
