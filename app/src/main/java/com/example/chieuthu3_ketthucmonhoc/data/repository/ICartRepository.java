package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.List;

public interface ICartRepository {
    List<CartItem> getCartItems();
    CartItem addItem(MenuItem item, int quantity);
    CartItem addItem(MenuItem item);
    void updateQuantity(int menuItemId, int quantity);
    void removeItem(int menuItemId);
    void clearCart();
    long getTotalAmount();
    int getTotalItemCount();
}
