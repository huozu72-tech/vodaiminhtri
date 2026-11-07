package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderRepositoryImpl implements IOrderRepository {

    private final CopyOnWriteArrayList<Order> orderList;

    public OrderRepositoryImpl() {
        this(InitialDataFactory.createInitialMenuItems());
    }

    public OrderRepositoryImpl(List<MenuItem> menuItems) {
        this(menuItems, InitialDataFactory.createInitialOrders(menuItems));
    }

    public OrderRepositoryImpl(List<MenuItem> menuItems, List<Order> initialOrders) {
        this.orderList = new CopyOnWriteArrayList<>(initialOrders != null ? initialOrders : new ArrayList<>());
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>(orderList);
        list.sort(Comparator.comparingLong(Order::getCreatedAt).reversed());
        return list;
    }

    @Override
    public Order getOrderById(String orderId) {
        if (orderId == null) return null;
        for (Order order : orderList) {
            if (order.getOrderId().equalsIgnoreCase(orderId.trim())) {
                return order;
            }
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> result = new ArrayList<>();
        for (Order order : orderList) {
            if (order.getCustomerId() != null && order.getCustomerId() == customerId) {
                result.add(order);
            }
        }
        result.sort(Comparator.comparingLong(Order::getCreatedAt).reversed());
        return result;
    }

    @Override
    public synchronized Order saveOrder(Order order) {
        int existingIdx = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId().equalsIgnoreCase(order.getOrderId())) {
                existingIdx = i;
                break;
            }
        }

        if (existingIdx >= 0) {
            orderList.set(existingIdx, order);
        } else {
            orderList.add(0, order);
        }
        return order;
    }

    @Override
    public boolean updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return false;
        }
        Order updated = new Order(
                order.getOrderId(),
                order.getTableNumber(),
                order.getCustomerId(),
                order.getItems(),
                order.getTotalAmount(),
                newStatus,
                order.getPaymentMethod(),
                order.getQrPayloadUrl(),
                order.getCreatedAt()
        );
        saveOrder(updated);
        return true;
    }
}
