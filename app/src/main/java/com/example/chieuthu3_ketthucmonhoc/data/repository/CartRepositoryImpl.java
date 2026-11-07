package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CartRepositoryImpl implements ICartRepository {

    private final CopyOnWriteArrayList<CartItem> cartItems = new CopyOnWriteArrayList<>();

    @Override
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    @Override
    public CartItem addItem(MenuItem item, int quantity) {
        for (CartItem existing : cartItems) {
            if (existing.getMenuItem().getId() == item.getId()) {
                existing.setQuantity(existing.getQuantity() + quantity);
                return existing;
            }
        }
        CartItem newEntry = new CartItem(item, quantity);
        cartItems.add(newEntry);
        return newEntry;
    }

    @Override
    public CartItem addItem(MenuItem item) {
        return addItem(item, 1);
    }

    @Override
    public void updateQuantity(int menuItemId, int quantity) {
        if (quantity <= 0) {
            removeItem(menuItemId);
            return;
        }
        for (CartItem existing : cartItems) {
            if (existing.getMenuItem().getId() == menuItemId) {
                existing.setQuantity(quantity);
                break;
            }
        }
    }

    @Override
    public void updateNote(int menuItemId, String note) {
        for (CartItem existing : cartItems) {
            if (existing.getMenuItem().getId() == menuItemId) {
                existing.setNote(note);
                break;
            }
        }
    }

    @Override
    public void removeItem(int menuItemId) {
        cartItems.removeIf(item -> item.getMenuItem().getId() == menuItemId);
    }

    @Override
    public void clearCart() {
        cartItems.clear();
    }

    @Override
    public long getTotalAmount() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    @Override
    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}
