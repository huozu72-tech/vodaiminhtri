package com.example.chieuthu3_ketthucmonhoc.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.Collections;

@Entity(
    tableName = "orders",
    foreignKeys = @ForeignKey(
        entity = CustomerEntity.class,
        parentColumns = "id",
        childColumns = "customer_id",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = @Index("customer_id")
)
public class OrderEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "order_id")
    private String orderId;

    private String tableNumber;

    @ColumnInfo(name = "customer_id")
    private Integer customerId;

    private long totalAmount;
    private String status;
    private String paymentMethod;
    private String qrPayloadUrl;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public OrderEntity(@NonNull String orderId, String tableNumber, Integer customerId, long totalAmount,
                       String status, String paymentMethod, String qrPayloadUrl, long createdAt) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.qrPayloadUrl = qrPayloadUrl;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull String orderId) {
        this.orderId = orderId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getQrPayloadUrl() {
        return qrPayloadUrl;
    }

    public void setQrPayloadUrl(String qrPayloadUrl) {
        this.qrPayloadUrl = qrPayloadUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Order toDomain() {
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status);
        } catch (Exception e) {
            orderStatus = OrderStatus.PENDING;
        }
        return new Order(
                orderId,
                tableNumber,
                customerId,
                Collections.emptyList(),
                totalAmount,
                orderStatus,
                paymentMethod,
                qrPayloadUrl,
                createdAt
        );
    }

    public static OrderEntity fromDomain(Order order) {
        return new OrderEntity(
                order.getOrderId().trim(),
                order.getTableNumber(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                order.getQrPayloadUrl(),
                order.getCreatedAt()
        );
    }
}
