package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.CartRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.services.CartServiceImpl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CartServiceTest {

    private CartServiceImpl cartService;
    private CartRepositoryImpl cartRepository;

    private final MenuItem food1 = new MenuItem(1, "Chân Giò Muối Chiên Giòn", "Thơm ngon", 120000L, FoodCategory.MAIN_DISH);
    private final MenuItem food2 = new MenuItem(2, "Coca Cola", "Nước ngọt", 15000L, FoodCategory.BEVERAGE);

    @Before
    public void setup() {
        cartRepository = new CartRepositoryImpl();
        cartService = new CartServiceImpl(cartRepository);
    }

    @Test
    public void cartStartsEmpty() {
        assertTrue(cartService.isEmpty());
        assertEquals(0, cartService.getTotalItemCount());
        assertEquals(0L, cartService.getTotalAmount());
    }

    @Test
    public void addingItemsIncreasesCountAndCalculatesTotal() {
        cartService.addItem(food1, 1);
        assertFalse(cartService.isEmpty());
        assertEquals(1, cartService.getTotalItemCount());
        assertEquals(120000L, cartService.getTotalAmount());

        cartService.addItem(food2, 2);
        assertEquals(3, cartService.getTotalItemCount());
        assertEquals(150000L, cartService.getTotalAmount());
    }

    @Test
    public void addingDuplicateItemIncrementsQuantity() {
        cartService.addItem(food1, 1);
        cartService.addItem(food1, 2);

        assertEquals(1, cartService.getCartItems().size());
        assertEquals(3, cartService.getCartItems().get(0).getQuantity());
        assertEquals(360000L, cartService.getTotalAmount());
    }

    @Test
    public void updateQuantityModifiesCountOrRemovesWhenZero() {
        cartService.addItem(food1, 2);
        cartService.updateQuantity(food1.getId(), 5);
        assertEquals(5, cartService.getTotalItemCount());

        cartService.updateQuantity(food1.getId(), 0);
        assertTrue(cartService.isEmpty());
    }

    @Test
    public void removeItemAndClearCartWorkAsExpected() {
        cartService.addItem(food1, 1);
        cartService.addItem(food2, 1);
        assertEquals(2, cartService.getCartItems().size());

        cartService.removeItem(food1.getId());
        assertEquals(1, cartService.getCartItems().size());
        assertEquals(food2.getId(), cartService.getCartItems().get(0).getMenuItem().getId());

        cartService.clearCart();
        assertTrue(cartService.isEmpty());
    }

    @Test
    public void updateNoteSavesTrimmedKitchenNotePerCartItem() {
        cartService.addItem(food1, 2);
        assertEquals("", cartService.getCartItems().get(0).getNote());

        cartService.updateNote(food1.getId(), "  Ít cay, không hành  ");
        assertEquals("Ít cay, không hành", cartService.getCartItems().get(0).getNote());

        cartService.updateNote(food1.getId(), null);
        assertEquals("", cartService.getCartItems().get(0).getNote());
    }
}
