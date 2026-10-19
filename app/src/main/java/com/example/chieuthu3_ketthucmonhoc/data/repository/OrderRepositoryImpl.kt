package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import java.util.concurrent.CopyOnWriteArrayList

class OrderRepositoryImpl(
    menuItems: List<MenuItem> = InitialDataFactory.createInitialMenuItems(),
    initialOrders: List<Order> = InitialDataFactory.createInitialOrders(menuItems)
) : IOrderRepository {

    private val orderList = CopyOnWriteArrayList<Order>(initialOrders)

    override fun getAllOrders(): List<Order> = orderList.sortedByDescending { it.createdAt }

    override fun getOrderById(orderId: String): Order? {
        return orderList.find { it.orderId.equals(orderId.trim(), ignoreCase = true) }
    }

    override fun getOrdersByCustomer(customerId: Int): List<Order> {
        return orderList.filter { it.customerId == customerId }.sortedByDescending { it.createdAt }
    }

    override fun saveOrder(order: Order): Order {
        val existingIdx = orderList.indexOfFirst { it.orderId == order.orderId }
        if (existingIdx >= 0) {
            orderList[existingIdx] = order
        } else {
            orderList.add(0, order)
        }
        return order
    }

    override fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        val order = getOrderById(orderId) ?: return false
        val updated = order.copy(status = newStatus)
        saveOrder(updated)
        return true
    }
}
