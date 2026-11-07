package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.LoyaltyProfile;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.PromoVoucher;
import com.example.chieuthu3_ketthucmonhoc.domain.models.RestaurantTable;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableServiceRequest;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.services.RestaurantOperationsServiceImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RestaurantOperationsServiceTest {

    private RestaurantOperationsServiceImpl opsService;
    private MenuRepositoryImpl menuRepository;
    private OrderRepositoryImpl orderRepository;

    @Before
    public void setup() {
        List<MenuItem> menu = InitialDataFactory.createInitialMenuItems();
        menuRepository = new MenuRepositoryImpl(menu);
        orderRepository = new OrderRepositoryImpl(menu, InitialDataFactory.createInitialOrders(menu));
        opsService = new RestaurantOperationsServiceImpl(menuRepository, orderRepository);
    }

    @Test
    public void voucherCalculationAndToggleActiveWorksCorrectly() {
        PromoVoucher voucher = opsService.getVoucherByCode("CHIEUTHU3");
        assertNotNull(voucher);
        assertTrue(voucher.isActive());

        // 15% of 200,000 = 30,000
        long discount = opsService.calculateVoucherDiscount("CHIEUTHU3", 200000L, 2);
        assertEquals(30000L, discount);

        // Below minimum order (100,000) -> 0 discount
        long ineligibleDiscount = opsService.calculateVoucherDiscount("CHIEUTHU3", 80000L, 2);
        assertEquals(0L, ineligibleDiscount);

        // Toggle active off -> 0 discount
        assertTrue(opsService.toggleVoucherStatus("CHIEUTHU3"));
        assertEquals(0L, opsService.calculateVoucherDiscount("CHIEUTHU3", 200000L, 2));
    }

    @Test
    public void tableReservationAndStatusTransitionsWorkCorrectly() {
        List<RestaurantTable> tables = opsService.getAllTables();
        assertEquals(10, tables.size());

        boolean reserved = opsService.reserveTable(
                "Bàn 02",
                "Trần Thị B",
                "0912345678",
                "19:30 Hôm nay",
                4,
                "Sinh nhật gia đình"
        );
        assertTrue(reserved);

        RestaurantTable table2 = opsService.getTableByNumber("Bàn 02");
        assertNotNull(table2);
        assertEquals(TableStatus.RESERVED, table2.getStatus());
        assertEquals("Trần Thị B", table2.getReservedByName());

        assertTrue(opsService.occupyTable("Bàn 02", 4));
        assertEquals(TableStatus.OCCUPIED, opsService.getTableByNumber("Bàn 02").getStatus());

        assertTrue(opsService.markTableAvailable("Bàn 02"));
        assertEquals(TableStatus.AVAILABLE, opsService.getTableByNumber("Bàn 02").getStatus());
    }

    @Test
    public void waiterServiceRequestLifecycleWorksCorrectly() {
        int initialPending = opsService.getPendingServiceRequests().size();
        TableServiceRequest req = opsService.createServiceRequest("Bàn 05", "Thêm đá / Khăn giấy", "Cho 2 ly trà đá");
        assertNotNull(req);
        assertFalse(req.isResolved());
        assertEquals(initialPending + 1, opsService.getPendingServiceRequests().size());

        assertTrue(opsService.resolveServiceRequest(req.getId()));
        assertEquals(initialPending, opsService.getPendingServiceRequests().size());
    }

    @Test
    public void favoriteDishWishlistToggleWorksCorrectly() {
        assertFalse(opsService.isFavoriteDish(5));
        assertTrue(opsService.toggleFavoriteDish(5));
        assertTrue(opsService.isFavoriteDish(5));

        assertFalse(opsService.toggleFavoriteDish(5));
        assertFalse(opsService.isFavoriteDish(5));
    }

    @Test
    public void customerLoyaltyProfileCalculatesTierAndPointsAccurately() {
        // Customer 1 has completed orders in InitialDataFactory
        LoyaltyProfile profile = opsService.getCustomerLoyaltyProfile(1);
        assertNotNull(profile);
        assertTrue(profile.getTotalSpent() > 0);
        assertTrue(profile.getPoints() > 0);
        assertNotNull(profile.getTierName());
    }

    @Test
    public void generateAnalyticalReportContainsRevenueAndTableOccupancy() {
        String report = opsService.generateAnalyticalReportText();
        assertNotNull(report);
        assertTrue(report.contains("BÁO CÁO PHÂN TÍCH VẬN HÀNH & DOANH THU"));
        assertTrue(report.contains("TỔNG QUAN DOANH THU & ĐƠN HÀNG"));
        assertTrue(report.contains("CÔNG SUẤT BÀN & PHỤC VỤ TẠI CHỖ"));
        assertTrue(report.contains("CHƯƠNG TRÌNH KHUYẾN MÃI (VOUCHER)"));
    }
}
