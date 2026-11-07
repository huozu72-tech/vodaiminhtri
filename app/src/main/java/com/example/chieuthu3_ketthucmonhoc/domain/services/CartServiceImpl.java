package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.ICartRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.List;

public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private String appliedVoucherCode = "";

    public CartServiceImpl(ICartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public CartItem addItem(MenuItem item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
        }
        if (item == null || !item.isAvailable()) {
            throw new IllegalArgumentException("Món ăn hiện không khả dụng");
        }
        return cartRepository.addItem(item, quantity);
    }

    @Override
    public CartItem addItem(MenuItem item) {
        return addItem(item, 1);
    }

    @Override
    public void updateQuantity(int menuItemId, int quantity) {
        cartRepository.updateQuantity(menuItemId, quantity);
    }

    @Override
    public void updateNote(int menuItemId, String note) {
        cartRepository.updateNote(menuItemId, note);
    }

    @Override
    public void removeItem(int menuItemId) {
        cartRepository.removeItem(menuItemId);
    }

    @Override
    public void clearCart() {
        cartRepository.clearCart();
        appliedVoucherCode = "";
    }

    @Override
    public List<CartItem> getCartItems() {
        return cartRepository.getCartItems();
    }

    @Override
    public long getTotalAmount() {
        return cartRepository.getTotalAmount();
    }

    @Override
    public int getTotalItemCount() {
        return cartRepository.getTotalItemCount();
    }

    @Override
    public boolean isEmpty() {
        return cartRepository.getCartItems().isEmpty();
    }

    @Override
    public String getAppliedVoucherCode() {
        return appliedVoucherCode;
    }

    @Override
    public void setAppliedVoucherCode(String code) {
        this.appliedVoucherCode = code != null ? code.trim().toUpperCase() : "";
    }
}
