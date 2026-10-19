package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import java.util.concurrent.CopyOnWriteArrayList

class CartRepositoryImpl : ICartRepository {

    private val cartItems = CopyOnWriteArrayList<CartItem>()

    override fun getCartItems(): List<CartItem> = cartItems.toList()

    override fun addItem(item: MenuItem, quantity: Int): CartItem {
        val existing = cartItems.find { it.menuItem.id == item.id }
        return if (existing != null) {
            existing.quantity += quantity
            existing
        } else {
            val newEntry = CartItem(item, quantity)
            cartItems.add(newEntry)
            newEntry
        }
    }

    override fun updateQuantity(menuItemId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeItem(menuItemId)
            return
        }
        val existing = cartItems.find { it.menuItem.id == menuItemId }
        existing?.quantity = quantity
    }

    override fun removeItem(menuItemId: Int) {
        cartItems.removeIf { it.menuItem.id == menuItemId }
    }

    override fun clearCart() {
        cartItems.clear()
    }

    override fun getTotalAmount(): Long {
        return cartItems.sumOf { it.subtotal }
    }

    override fun getTotalItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }
}
