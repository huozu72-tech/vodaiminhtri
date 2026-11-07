package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.List;

public interface IOrderService {
    Order createOrder(String tableNumber, Integer customerId, List<CartItem> cartItems, String paymentMethod);
    Order createOrder(String tableNumber, Integer customerId, List<CartItem> cartItems);
    String generatePaymentQrUrl(Order order);
    Order getOrderById(String orderId);
    List<Order> getCustomerOrders(int customerId);
    List<Order> getAllOrders();
    boolean updateOrderStatus(String orderId, OrderStatus newStatus);
    boolean cancelOrder(String orderId);
}
