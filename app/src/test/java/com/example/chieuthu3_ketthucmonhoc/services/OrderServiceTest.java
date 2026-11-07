package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.services.OrderServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.QrBitmapGenerator;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class OrderServiceTest {

    private OrderServiceImpl orderService;
    private OrderRepositoryImpl orderRepository;

    private final MenuItem food1 = new MenuItem(1, "Chân Giò Muối Chiên Giòn", "Thơm ngon", 120000L, FoodCategory.MAIN_DISH);
    private final MenuItem food2 = new MenuItem(2, "7Up", "Nước ngọt", 15000L, FoodCategory.BEVERAGE);

    @Before
    public void setup() {
        orderRepository = new OrderRepositoryImpl(
                Arrays.asList(food1, food2),
                Collections.emptyList()
        );
        orderService = new OrderServiceImpl(orderRepository);
    }

    @Test
    public void createOrderCalculatesTotalAndGeneratesQrUrl() {
        List<CartItem> cartItems = Arrays.asList(
                new CartItem(food1, 1),
                new CartItem(food2, 2)
        );

        Order order = orderService.createOrder("Bàn 08", 1, cartItems);
        assertNotNull(order.getOrderId());
        assertTrue(order.getOrderId().startsWith("#ORD-"));
        assertEquals("Bàn 08", order.getTableNumber());
        assertEquals(150000L, order.getTotalAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getQrPayloadUrl());
        assertTrue(order.getQrPayloadUrl().contains("vietqr.io"));
        assertTrue(order.getQrPayloadUrl().contains("amount=150000"));
    }

    @Test
    public void orderStatusUpdatesCorrectly() {
        List<CartItem> cartItems = Collections.singletonList(new CartItem(food1, 1));
        Order order = orderService.createOrder("Bàn 01", 1, cartItems);

        boolean updated = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PREPARING);
        assertTrue(updated);
        assertEquals(OrderStatus.PREPARING, orderService.getOrderById(order.getOrderId()).getStatus());

        orderService.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED);
        assertEquals(OrderStatus.COMPLETED, orderService.getOrderById(order.getOrderId()).getStatus());

        // Cannot cancel completed order
        boolean cancelled = orderService.cancelOrder(order.getOrderId());
        assertFalse(cancelled);
        assertEquals(OrderStatus.COMPLETED, orderService.getOrderById(order.getOrderId()).getStatus());
    }

    @Test
    public void orderCodesNeverDuplicateExistingOrPreviouslyCreatedOrders() {
        OrderRepositoryImpl seededRepository = new OrderRepositoryImpl(
                Arrays.asList(food1, food2),
                Arrays.asList(
                        new Order("#ORD-1023", "Bàn 1", 1, Collections.singletonList(new OrderItem(1, food1, 1))),
                        new Order("#ORD-1025", "Bàn 2", 2, Collections.singletonList(new OrderItem(1, food2, 2)))
                )
        );
        OrderServiceImpl seededService = new OrderServiceImpl(seededRepository);

        // Mã mới phải lớn hơn mã seed lớn nhất thay vì ngẫu nhiên có nguy cơ trùng
        Order first = seededService.createOrder("Bàn 05", 1, Collections.singletonList(new CartItem(food1, 1)));
        assertEquals("#ORD-1026", first.getOrderId());

        Order second = seededService.createOrder("Bàn 06", 1, Collections.singletonList(new CartItem(food2, 1)));
        Order third = seededService.createOrder("Bàn 07", 1, Collections.singletonList(new CartItem(food2, 1)));
        assertNotEquals(second.getOrderId(), third.getOrderId());

        // Không đơn nào bị ghi đè: đủ 2 đơn seed + 3 đơn mới
        assertEquals(5, seededService.getAllOrders().size());
    }

    @Test
    public void orderStatusCannotJumpFromPendingStraightToCompleted() {
        Order order = orderService.createOrder("Bàn 02", 1, Collections.singletonList(new CartItem(food1, 1)));

        boolean jumped = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED);
        assertFalse(jumped);
        assertEquals(OrderStatus.PENDING, orderService.getOrderById(order.getOrderId()).getStatus());
    }

    @Test
    public void completedOrderStatusIsFinalAndCannotGoBackwards() {
        Order order = orderService.createOrder("Bàn 03", 1, Collections.singletonList(new CartItem(food1, 1)));
        assertTrue(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PREPARING));
        assertTrue(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED));

        assertFalse(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PENDING));
        assertFalse(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PREPARING));
        assertFalse(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.CANCELLED));
        assertEquals(OrderStatus.COMPLETED, orderService.getOrderById(order.getOrderId()).getStatus());
    }

    @Test
    public void cancelledOrderCannotBeCancelledAgainOrRevived() {
        Order order = orderService.createOrder("Bàn 04", 1, Collections.singletonList(new CartItem(food1, 1)));

        assertTrue(orderService.cancelOrder(order.getOrderId()));
        assertEquals(OrderStatus.CANCELLED, orderService.getOrderById(order.getOrderId()).getStatus());

        assertFalse(orderService.cancelOrder(order.getOrderId()));
        assertFalse(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PREPARING));
        assertFalse(orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PENDING));
        assertEquals(OrderStatus.CANCELLED, orderService.getOrderById(order.getOrderId()).getStatus());
    }

    @Test
    public void updateStatusOfUnknownOrderReturnsFalse() {
        assertFalse(orderService.updateOrderStatus("#ORD-KHONG-TON-TAI", OrderStatus.PREPARING));
    }

    @Test
    public void repositorySaveOrderWithDifferentCaseIdUpdatesInsteadOfDuplicating() {
        Order original = new Order(
                "#ORD-2000",
                "Bàn 1",
                1,
                Collections.singletonList(new OrderItem(1, food1, 1)),
                food1.getPrice(),
                OrderStatus.PENDING
        );
        orderRepository.saveOrder(original);

        Order updated = new Order(
                "#ord-2000",
                "Bàn 1",
                1,
                Collections.singletonList(new OrderItem(1, food1, 1)),
                food1.getPrice(),
                OrderStatus.PREPARING
        );
        orderRepository.saveOrder(updated);

        assertEquals(1, orderRepository.getAllOrders().size());
        assertEquals(OrderStatus.PREPARING, orderRepository.getOrderById("#ORD-2000").getStatus());
    }

    @Test
    public void vietQrEmvPayloadContainsValidNapas247StructureAndCrc16() {
        String emv = QrBitmapGenerator.buildVietQrEmvPayload("970422", "0901234567", 210000L, "TT BAN 12 210K");
        assertNotNull(emv);
        assertTrue(emv.startsWith("00020101021238"));
        assertTrue(emv.contains("A000000727")); // Napas247 GUID
        assertTrue(emv.contains("970422"));     // MBBank BIN
        assertTrue(emv.contains("0901234567")); // Account Number
        assertTrue(emv.contains("QRIBFTTA"));   // Fast transfer service code
        assertTrue(emv.contains("5303704"));    // VND Currency code
        assertTrue(emv.contains("5406210000")); // Exact transaction amount
        assertTrue(emv.contains("5802VN"));     // Country VN
        assertTrue(emv.contains("6304"));       // CRC-16 tag

        // Kiểm tra 4 ký tự cuối là mã HEX CRC-16/CCITT-FALSE chuẩn
        String withoutCrcVal = emv.substring(0, emv.length() - 4);
        String expectedCrc = QrBitmapGenerator.computeCrc16CcittHex(withoutCrcVal);
        assertEquals(4, expectedCrc.length());
        assertTrue(emv.endsWith(expectedCrc));
    }
}
