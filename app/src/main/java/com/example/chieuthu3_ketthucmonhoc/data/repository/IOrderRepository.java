package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.List;

public interface IOrderRepository {
    List<Order> getAllOrders();
    Order getOrderById(String orderId);
    List<Order> getOrdersByCustomer(int customerId);
    Order saveOrder(Order order);
    boolean updateOrderStatus(String orderId, OrderStatus newStatus);
}
