package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardServiceImpl implements IDashboardService {

    private final IMenuRepository menuRepository;
    private final IOrderRepository orderRepository;

    public DashboardServiceImpl(IMenuRepository menuRepository, IOrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public DashboardMetrics getDashboardMetrics() {
        List<Order> allOrders = orderRepository.getAllOrders();
        List<MenuItem> allMenuItems = menuRepository.getAllMenuItems();

        List<Order> completedOrders = new ArrayList<>();
        List<Order> pendingOrders = new ArrayList<>();

        for (Order order : allOrders) {
            if (order.getStatus() == OrderStatus.COMPLETED) {
                completedOrders.add(order);
            } else if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PREPARING) {
                pendingOrders.add(order);
            }
        }

        long totalRevenue = 0;
        for (Order order : completedOrders) {
            totalRevenue += order.getTotalAmount();
        }

        int totalCount = allOrders.size();
        int completedCount = completedOrders.size();
        int completionRate = totalCount > 0 ? (completedCount * 100) / totalCount : 0;

        return new DashboardMetrics(
                totalRevenue,
                totalCount,
                completedCount,
                pendingOrders.size(),
                allMenuItems.size(),
                completionRate
        );
    }

    @Override
    public List<TopSellingItem> getTopSellingMenuItems(int limit) {
        List<Order> allOrders = orderRepository.getAllOrders();
        Map<Integer, Integer> itemCounts = new HashMap<>();

        for (Order order : allOrders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            for (OrderItem orderItem : order.getItems()) {
                if (orderItem.getMenuItem() != null) {
                    int menuItemId = orderItem.getMenuItem().getId();
                    itemCounts.put(menuItemId, itemCounts.getOrDefault(menuItemId, 0) + orderItem.getQuantity());
                }
            }
        }

        List<TopSellingItem> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : itemCounts.entrySet()) {
            MenuItem menuItem = menuRepository.getMenuItemById(entry.getKey());
            if (menuItem != null) {
                result.add(new TopSellingItem(menuItem, entry.getValue()));
            }
        }

        result.sort((a, b) -> {
            int cmp = Integer.compare(b.getQuantitySold(), a.getQuantitySold());
            if (cmp != 0) return cmp;
            long revA = a.getMenuItem().getPrice() * a.getQuantitySold();
            long revB = b.getMenuItem().getPrice() * b.getQuantitySold();
            return Long.compare(revB, revA);
        });

        if (result.size() <= limit) {
            return result;
        }
        return new ArrayList<>(result.subList(0, limit));
    }

    @Override
    public List<TopSellingItem> getTopSellingMenuItems() {
        return getTopSellingMenuItems(5);
    }
}
