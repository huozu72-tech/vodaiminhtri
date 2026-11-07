package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.List;

public interface ICartService {
    CartItem addItem(MenuItem item, int quantity);
    CartItem addItem(MenuItem item);
    void updateQuantity(int menuItemId, int quantity);
    void removeItem(int menuItemId);
    void clearCart();
    List<CartItem> getCartItems();
    long getTotalAmount();
    int getTotalItemCount();
    boolean isEmpty();
}
