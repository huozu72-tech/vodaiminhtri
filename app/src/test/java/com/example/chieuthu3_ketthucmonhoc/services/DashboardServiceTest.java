package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardMetrics;
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.TopSellingItem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DashboardServiceTest {

    private DashboardServiceImpl dashboardService;
    private MenuRepositoryImpl menuRepository;
    private OrderRepositoryImpl orderRepository;

    private final MenuItem dish1 = new MenuItem(1, "Chân Giò Muối Chiên Giòn", "", 120000L, FoodCategory.MAIN_DISH);
    private final MenuItem dish2 = new MenuItem(2, "Coca Cola", "", 15000L, FoodCategory.BEVERAGE);

    @Before
    public void setup() {
        menuRepository = new MenuRepositoryImpl(Arrays.asList(dish1, dish2));
        List<Order> orders = Arrays.asList(
                new Order("#ORD-1", "Bàn 1", 1, Arrays.asList(new OrderItem(1, dish1, 1), new OrderItem(2, dish2, 2)), 150000L, OrderStatus.COMPLETED),
                new Order("#ORD-2", "Bàn 2", 1, Arrays.asList(new OrderItem(3, dish1, 1)), 120000L, OrderStatus.PENDING),
                new Order("#ORD-3", "Bàn 3", 1, Arrays.asList(new OrderItem(4, dish2, 1)), 15000L, OrderStatus.CANCELLED)
        );
        orderRepository = new OrderRepositoryImpl(Arrays.asList(dish1, dish2), orders);
        dashboardService = new DashboardServiceImpl(menuRepository, orderRepository);
    }

    @Test
    public void dashboardMetricsCalculatesRevenueAndCompletionRateCorrectly() {
        DashboardMetrics metrics = dashboardService.getDashboardMetrics();
        // Only completed orders contribute to total revenue
        assertEquals(150000L, metrics.getTotalRevenue());
        assertEquals(3, metrics.getTotalOrdersCount());
        assertEquals(1, metrics.getCompletedOrdersCount());
        assertEquals(1, metrics.getPendingOrdersCount());
        assertEquals(2, metrics.getTotalMenuItemsCount());
        assertEquals(33, metrics.getCompletionRatePercent()); // 1 out of 3 = 33%
        assertTrue(metrics.getFormattedRevenue().contains("150.000"));
    }

    @Test
    public void topSellingItemsSortsByTotalQuantityOrdered() {
        List<TopSellingItem> top = dashboardService.getTopSellingMenuItems(2);
        assertEquals(2, top.size());
        // dish2 was ordered: 2 (in order 1) + 1 (in order 3) = 3
        // dish1 was ordered: 1 (in order 1) + 1 (in order 2) = 2
        assertEquals(dish2.getId(), top.get(0).getMenuItem().getId());
        assertEquals(3, top.get(0).getQuantitySold());
        assertEquals(dish1.getId(), top.get(1).getMenuItem().getId());
        assertEquals(2, top.get(1).getQuantitySold());
    }
}
