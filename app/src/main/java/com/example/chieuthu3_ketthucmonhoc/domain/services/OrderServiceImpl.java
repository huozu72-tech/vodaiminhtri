package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements IOrderService {

    private static final String ORDER_CODE_PREFIX = "#ORD-";
    private static final long DEFAULT_ORDER_NUMBER = 999L;

    private final IOrderRepository orderRepository;

    public OrderServiceImpl(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order createOrder(String tableNumber, Integer customerId, List<CartItem> cartItems, String paymentMethod) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Không thể tạo đơn với giỏ hàng rỗng");
        }
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số bàn không được để trống");
        }

        String orderCode = generateUniqueOrderCode();
        List<OrderItem> orderItems = new ArrayList<>();
        int itemIndex = 1;
        long totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    itemIndex++,
                    cartItem.getMenuItem(),
                    cartItem.getQuantity(),
                    cartItem.getMenuItem().getPrice()
            );
            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();
        }

        String safePaymentMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty())
                ? paymentMethod.trim()
                : "Chuyển khoản QR";

        Order newOrder = new Order(
                orderCode,
                tableNumber.trim(),
                customerId,
                orderItems,
                totalAmount,
                OrderStatus.PENDING,
                safePaymentMethod,
                generatePaymentQrUrlInternal(orderCode, totalAmount),
                System.currentTimeMillis()
        );

        return orderRepository.saveOrder(newOrder);
    }

    @Override
    public Order createOrder(String tableNumber, Integer customerId, List<CartItem> cartItems) {
        return createOrder(tableNumber, customerId, cartItems, "Chuyển khoản QR");
    }

    @Override
    public String generatePaymentQrUrl(Order order) {
        if (order == null) return "";
        return generatePaymentQrUrlInternal(order.getOrderId(), order.getTotalAmount());
    }

    private String generatePaymentQrUrlInternal(String orderId, long amount) {
        String bankId = "MB";
        String accountNo = "0901234567";
        String template = "compact2";
        String cleanOrder = orderId != null ? orderId.replace("#", "") : "ORD";
        String description;
        try {
            description = URLEncoder.encode("THANH TOAN " + cleanOrder, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            description = "THANH+TOAN+" + cleanOrder;
        }
        return "https://img.vietqr.io/image/" + bankId + "-" + accountNo + "-" + template
                + ".png?amount=" + amount + "&addInfo=" + description + "&accountName=NHA%20HANG%20CHIEU%20THU%203";
    }

    private synchronized String generateUniqueOrderCode() {
        List<Order> allOrders = orderRepository.getAllOrders();
        long maxExistingNumber = DEFAULT_ORDER_NUMBER;
        for (Order order : allOrders) {
            if (order.getOrderId().startsWith(ORDER_CODE_PREFIX)) {
                try {
                    long num = Long.parseLong(order.getOrderId().substring(ORDER_CODE_PREFIX.length()));
                    if (num > maxExistingNumber) {
                        maxExistingNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        long nextNumber = maxExistingNumber + 1;
        while (orderRepository.getOrderById(ORDER_CODE_PREFIX + nextNumber) != null) {
            nextNumber++;
        }
        return ORDER_CODE_PREFIX + nextNumber;
    }

    @Override
    public Order getOrderById(String orderId) {
        return orderRepository.getOrderById(orderId);
    }

    @Override
    public List<Order> getCustomerOrders(int customerId) {
        return orderRepository.getOrdersByCustomer(customerId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    @Override
    public boolean updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            return false;
        }
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            return false;
        }
        return orderRepository.updateOrderStatus(orderId, newStatus);
    }

    @Override
    public boolean cancelOrder(String orderId) {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARING) {
            return false;
        }
        return orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) return false;
        switch (current) {
            case PENDING:
                return next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING:
                return next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
            case COMPLETED:
            case CANCELLED:
            default:
                return false;
        }
    }
}
