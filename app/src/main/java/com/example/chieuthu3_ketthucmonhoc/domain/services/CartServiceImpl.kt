package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.ICartRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

class CartServiceImpl(
    private val cartRepository: ICartRepository
) : ICartService {

    override fun addItem(item: MenuItem, quantity: Int): CartItem {
        require(quantity > 0) { "Số lượng món ăn phải lớn hơn 0" }
        require(item.isAvailable) { "Món ăn hiện không khả dụng" }
        return cartRepository.addItem(item, quantity)
    }

    override fun updateQuantity(menuItemId: Int, quantity: Int) {
        cartRepository.updateQuantity(menuItemId, quantity)
    }

    override fun removeItem(menuItemId: Int) {
        cartRepository.removeItem(menuItemId)
    }

    override fun clearCart() {
        cartRepository.clearCart()
    }

    override fun getCartItems(): List<CartItem> {
        return cartRepository.getCartItems()
    }

    override fun getTotalAmount(): Long {
        return cartRepository.getTotalAmount()
    }

    override fun getTotalItemCount(): Int {
        return cartRepository.getTotalItemCount()
    }

    override fun isEmpty(): Boolean {
        return cartRepository.getCartItems().isEmpty()
    }
}
