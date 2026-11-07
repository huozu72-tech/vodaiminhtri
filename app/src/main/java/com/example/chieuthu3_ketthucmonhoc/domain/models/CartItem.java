package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class CartItem {
    private final MenuItem menuItem;
    private int quantity;
    private String note;

    public CartItem(MenuItem menuItem, int quantity) {
        this(menuItem, quantity, "");
    }

    public CartItem(MenuItem menuItem, int quantity, String note) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.note = note != null ? note.trim() : "";
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note != null ? note.trim() : "";
    }

    public long getSubtotal() {
        return menuItem.getPrice() * quantity;
    }

    public String getFormattedSubtotal() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(getSubtotal()) + " đ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(menuItem, cartItem.menuItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItem);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "menuItem=" + menuItem.getName() +
                ", quantity=" + quantity +
                '}';
    }
}
