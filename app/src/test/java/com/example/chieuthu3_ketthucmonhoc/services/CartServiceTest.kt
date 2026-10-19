package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.CartRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.services.CartServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartServiceTest {

    private lateinit var cartService: CartServiceImpl
    private lateinit var cartRepository: CartRepositoryImpl

    private val food1 = MenuItem(1, "Chân Giò Muối Chiên Giòn", "Thơm ngon", 120000, FoodCategory.MAIN_DISH)
    private val food2 = MenuItem(2, "Coca Cola", "Nước ngọt", 15000, FoodCategory.BEVERAGE)

    @Before
    fun setup() {
        cartRepository = CartRepositoryImpl()
        cartService = CartServiceImpl(cartRepository)
    }

    @Test
    fun `cart starts empty`() {
        assertTrue(cartService.isEmpty())
        assertEquals(0, cartService.getTotalItemCount())
        assertEquals(0L, cartService.getTotalAmount())
    }

    @Test
    fun `adding items increases count and calculates total`() {
        cartService.addItem(food1, 1)
        assertFalse(cartService.isEmpty())
        assertEquals(1, cartService.getTotalItemCount())
        assertEquals(120000L, cartService.getTotalAmount())

        cartService.addItem(food2, 2)
        assertEquals(3, cartService.getTotalItemCount())
        assertEquals(150000L, cartService.getTotalAmount())
    }

    @Test
    fun `adding duplicate item increments quantity`() {
        cartService.addItem(food1, 1)
        cartService.addItem(food1, 2)

        assertEquals(1, cartService.getCartItems().size)
        assertEquals(3, cartService.getCartItems()[0].quantity)
        assertEquals(360000L, cartService.getTotalAmount())
    }

    @Test
    fun `updateQuantity modifies count or removes when zero`() {
        cartService.addItem(food1, 2)
        cartService.updateQuantity(food1.id, 5)
        assertEquals(5, cartService.getTotalItemCount())

        cartService.updateQuantity(food1.id, 0)
        assertTrue(cartService.isEmpty())
    }

    @Test
    fun `removeItem and clearCart work as expected`() {
        cartService.addItem(food1, 1)
        cartService.addItem(food2, 1)
        assertEquals(2, cartService.getCartItems().size)

        cartService.removeItem(food1.id)
        assertEquals(1, cartService.getCartItems().size)
        assertEquals(food2.id, cartService.getCartItems()[0].menuItem.id)

        cartService.clearCart()
        assertTrue(cartService.isEmpty())
    }
}
