package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class OrderItem {
    private final int id;
    private final MenuItem menuItem;
    private final int quantity;
    private final long unitPrice;

    public OrderItem(int id, MenuItem menuItem, int quantity) {
        this(id, menuItem, quantity, menuItem != null ? menuItem.getPrice() : 0);
    }

    public OrderItem(int id, MenuItem menuItem, int quantity, long unitPrice) {
        this.id = id;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() {
        return id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public long getSubtotal() {
        return unitPrice * quantity;
    }

    public String getFormattedSubtotal() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(getSubtotal()) + " đ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id == orderItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", menuItem=" + (menuItem != null ? menuItem.getName() : "null") +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
