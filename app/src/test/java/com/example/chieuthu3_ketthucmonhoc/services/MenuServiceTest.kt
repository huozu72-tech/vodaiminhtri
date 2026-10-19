package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute
import com.example.chieuthu3_ketthucmonhoc.domain.services.MenuServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MenuServiceTest {

    private lateinit var menuService: MenuServiceImpl
    private lateinit var menuRepository: MenuRepositoryImpl

    @Before
    fun setup() {
        val sampleItems = listOf(
            MenuItem(1, "Chân Giò Muối Chiên Giòn", "Món giòn đậm đà", 120000, FoodCategory.MAIN_DISH, listOf(TasteAttribute.CRISPY)),
            MenuItem(2, "Chân Gà Sả Tắc", "Chua cay sả tắc", 60000, FoodCategory.SIDE_DISH, listOf(TasteAttribute.SOUR, TasteAttribute.SPICY)),
            MenuItem(3, "7Up", "Nước ngọt giải khát", 15000, FoodCategory.BEVERAGE, listOf(TasteAttribute.REFRESHING))
        )
        menuRepository = MenuRepositoryImpl(sampleItems)
        menuService = MenuServiceImpl(menuRepository)
    }

    @Test
    fun `getAllMenuItems returns initial menu`() {
        val list = menuService.getAllMenuItems()
        assertEquals(3, list.size)
    }

    @Test
    fun `getMenuItemsByCategory filters correctly`() {
        val mainDishes = menuService.getMenuItemsByCategory(FoodCategory.MAIN_DISH)
        assertEquals(1, mainDishes.size)
        assertEquals("Chân Giò Muối Chiên Giòn", mainDishes[0].name)

        val drinks = menuService.getMenuItemsByCategory(FoodCategory.BEVERAGE)
        assertEquals(1, drinks.size)
        assertEquals("7Up", drinks[0].name)
    }

    @Test
    fun `searchMenuItems finds by accent and non-accent words`() {
        val res1 = menuService.searchMenuItems("chan gio")
        assertEquals(1, res1.size)
        assertEquals(1, res1[0].id)

        val res2 = menuService.searchMenuItems("chua cay")
        assertEquals(1, res2.size)
        assertEquals("Chân Gà Sả Tắc", res2[0].name)
    }

    @Test
    fun `create and delete menu item`() {
        val created = menuService.createMenuItem(
            name = "Bò Lúc Lắc",
            description = "Bò xào ớt chuông",
            price = 140000,
            category = FoodCategory.MAIN_DISH,
            tasteAttributes = listOf(TasteAttribute.SAVORY)
        )
        assertNotNull(created)
        assertTrue(created.id > 0)
        assertEquals(4, menuService.getAllMenuItems().size)

        val deleted = menuService.deleteMenuItem(created.id)
        assertTrue(deleted)
        assertEquals(3, menuService.getAllMenuItems().size)
    }

    @Test
    fun `toggleAvailability changes availability state`() {
        val updated = menuService.toggleAvailability(1)
        assertNotNull(updated)
        assertFalse(updated!!.isAvailable)
        assertFalse(menuService.getAvailableMenuItems().any { it.id == 1 })
    }
}
