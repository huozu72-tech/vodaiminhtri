package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.MenuServiceImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MenuServiceTest {

    private MenuServiceImpl menuService;
    private MenuRepositoryImpl menuRepository;

    @Before
    public void setup() {
        List<MenuItem> sampleItems = Arrays.asList(
                new MenuItem(1, "Chân Giò Muối Chiên Giòn", "Món giòn đậm đà", 120000L, FoodCategory.MAIN_DISH,
                        Collections.singletonList(TasteAttribute.CRISPY), true, 4.8, 0, 0),
                new MenuItem(2, "Chân Gà Sả Tắc", "Chua cay sả tắc", 60000L, FoodCategory.SIDE_DISH,
                        Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY), true, 4.8, 0, 0),
                new MenuItem(3, "7Up", "Nước ngọt giải khát", 15000L, FoodCategory.BEVERAGE,
                        Collections.singletonList(TasteAttribute.REFRESHING), true, 4.8, 0, 0)
        );
        menuRepository = new MenuRepositoryImpl(sampleItems);
        menuService = new MenuServiceImpl(menuRepository);
    }

    @Test
    public void getAllMenuItemsReturnsInitialMenu() {
        List<MenuItem> list = menuService.getAllMenuItems();
        assertEquals(3, list.size());
    }

    @Test
    public void getMenuItemsByCategoryFiltersCorrectly() {
        List<MenuItem> mainDishes = menuService.getMenuItemsByCategory(FoodCategory.MAIN_DISH);
        assertEquals(1, mainDishes.size());
        assertEquals("Chân Giò Muối Chiên Giòn", mainDishes.get(0).getName());

        List<MenuItem> drinks = menuService.getMenuItemsByCategory(FoodCategory.BEVERAGE);
        assertEquals(1, drinks.size());
        assertEquals("7Up", drinks.get(0).getName());
    }

    @Test
    public void searchMenuItemsFindsByAccentAndNonAccentWords() {
        List<MenuItem> res1 = menuService.searchMenuItems("chan gio");
        assertEquals(1, res1.size());
        assertEquals(1, res1.get(0).getId());

        List<MenuItem> res2 = menuService.searchMenuItems("chua cay");
        assertEquals(1, res2.size());
        assertEquals("Chân Gà Sả Tắc", res2.get(0).getName());
    }

    @Test
    public void createAndDeleteMenuItem() {
        MenuItem created = menuService.createMenuItem(
                "Bò Lúc Lắc",
                "Bò xào ớt chuông",
                140000L,
                FoodCategory.MAIN_DISH,
                Collections.singletonList(TasteAttribute.SAVORY)
        );
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(4, menuService.getAllMenuItems().size());

        boolean deleted = menuService.deleteMenuItem(created.getId());
        assertTrue(deleted);
        assertEquals(3, menuService.getAllMenuItems().size());
    }

    @Test
    public void toggleAvailabilityChangesAvailabilityState() {
        MenuItem updated = menuService.toggleAvailability(1);
        assertNotNull(updated);
        assertFalse(updated.isAvailable());

        boolean containsAvailable = false;
        for (MenuItem item : menuService.getAvailableMenuItems()) {
            if (item.getId() == 1) {
                containsAvailable = true;
                break;
            }
        }
        assertFalse(containsAvailable);
    }
}
