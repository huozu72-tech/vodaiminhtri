package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

interface ICartRepository {
    fun getCartItems(): List<CartItem>
    fun addItem(item: MenuItem, quantity: Int = 1): CartItem
    fun updateQuantity(menuItemId: Int, quantity: Int)
    fun removeItem(menuItemId: Int)
    fun clearCart()
    fun getTotalAmount(): Long
    fun getTotalItemCount(): Int
}
