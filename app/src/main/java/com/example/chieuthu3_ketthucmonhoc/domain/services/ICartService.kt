package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

interface ICartService {
    fun addItem(item: MenuItem, quantity: Int = 1): CartItem
    fun updateQuantity(menuItemId: Int, quantity: Int)
    fun removeItem(menuItemId: Int)
    fun clearCart()
    fun getCartItems(): List<CartItem>
    fun getTotalAmount(): Long
    fun getTotalItemCount(): Int
    fun isEmpty(): Boolean
}
