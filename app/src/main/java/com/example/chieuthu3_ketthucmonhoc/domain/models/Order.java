package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Order {
    private final String orderId;
    private final String tableNumber;
    private final Integer customerId;
    private final List<OrderItem> items;
    private final long totalAmount;
    private final OrderStatus status;
    private final String paymentMethod;
    private final String qrPayloadUrl;
    private final long createdAt;

    public Order(String orderId, String tableNumber, Integer customerId, List<OrderItem> items) {
        this(orderId, tableNumber, customerId, items, calculateTotal(items), OrderStatus.PENDING, "Chuyển khoản QR", null, System.currentTimeMillis());
    }

    public Order(String orderId, String tableNumber, Integer customerId, List<OrderItem> items, long totalAmount, OrderStatus status) {
        this(orderId, tableNumber, customerId, items, totalAmount, status, "Chuyển khoản QR", null, System.currentTimeMillis());
    }

    public Order(String orderId, String tableNumber, Integer customerId, List<OrderItem> items,
                 long totalAmount, OrderStatus status, String paymentMethod, String qrPayloadUrl, long createdAt) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.customerId = customerId;
        this.items = items != null ? Collections.unmodifiableList(new ArrayList<>(items)) : Collections.emptyList();
        this.totalAmount = totalAmount;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Chuyển khoản QR";
        this.qrPayloadUrl = qrPayloadUrl;
        this.createdAt = createdAt;
    }

    private static long calculateTotal(List<OrderItem> items) {
        if (items == null) return 0;
        long sum = 0;
        for (OrderItem item : items) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getQrPayloadUrl() {
        return qrPayloadUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getItemsSummary() {
        if (items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem it = items.get(i);
            sb.append(it.getMenuItem() != null ? it.getMenuItem().getName() : "Món")
              .append(" x")
              .append(it.getQuantity());
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getFormattedTotal() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(totalAmount) + " đ";
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM", new Locale("vi", "VN"));
        return sdf.format(new Date(createdAt));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", tableNumber='" + tableNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }
}
