package com.example.chieuthu3_ketthucmonhoc.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;

@Entity(
    tableName = "order_items",
    primaryKeys = {"order_id", "id"},
    foreignKeys = {
        @ForeignKey(
            entity = OrderEntity.class,
            parentColumns = "order_id",
            childColumns = "order_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = MenuItemEntity.class,
            parentColumns = "id",
            childColumns = "menu_item_id",
            onDelete = ForeignKey.RESTRICT
        )
    },
    indices = {
        @Index("order_id"),
        @Index("menu_item_id"),
        @Index(value = {"order_id", "menu_item_id"}, unique = true)
    }
)
public class OrderItemEntity {

    private int id;

    @NonNull
    @ColumnInfo(name = "order_id")
    private String orderId;

    @ColumnInfo(name = "menu_item_id")
    private int menuItemId;

    private int quantity;
    private long unitPrice;

    public OrderItemEntity(int id, @NonNull String orderId, int menuItemId, int quantity, long unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull String orderId) {
        this.orderId = orderId;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public OrderItem toDomain(MenuItem menuItem) {
        return new OrderItem(
                id,
                menuItem,
                quantity,
                unitPrice
        );
    }

    public static OrderItemEntity fromDomain(OrderItem item, String orderId) {
        return new OrderItemEntity(
                item.getId(),
                orderId,
                item.getMenuItem() != null ? item.getMenuItem().getId() : 0,
                item.getQuantity(),
                item.getUnitPrice()
        );
    }
}
