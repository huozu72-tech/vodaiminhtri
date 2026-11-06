package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Khóa hành vi của MenuRepositoryImpl in-memory (trước đây chưa có test),
 * đặc biệt là chuẩn hóa dấu trong searchMenuItems (bỏ dấu + đ->d) —
 * chính là hợp đồng mà màn tìm kiếm của khách đang dựa vào khi gõ không dấu.
 */
class MenuRepositoryImplTest {

    private lateinit var repository: MenuRepositoryImpl

    private val gaNuong = MenuItem(
        id = 1,
        name = "Gà Nướng Mật Ong",
        description = "Gà ta ướp mật ong nướng vàng óng",
        price = 150000,
        category = FoodCategory.MAIN_DISH
    )
    private val tomChien = MenuItem(
        id = 2,
        name = "Tôm Chiên Xù",
        description = "Tôm sú tẩm bột chiên xù",
        price = 95000,
        category = FoodCategory.MAIN_DISH
    )
    private val banhDuc = MenuItem(
        id = 3,
        name = "Bánh Đúc Nhân Thịt",
        description = "Bánh đúc mặn kèm nước mắm",
        price = 40000,
        category = FoodCategory.SIDE_DISH
    )
    private val coca = MenuItem(
        id = 4,
        name = "Coca Cola",
        description = "Nước ngọt có ga mát lạnh",
        price = 15000,
        category = FoodCategory.BEVERAGE
    )

    @Before
    fun setup() {
        repository = MenuRepositoryImpl(listOf(gaNuong, tomChien, banhDuc, coca))
    }

    @Test
    fun `search matches unaccented query against accented name`() {
        val results = repository.searchMenuItems("ga nuong")
        assertEquals(listOf(gaNuong), results)
    }

    @Test
    fun `search normalizes đ to d`() {
        val results = repository.searchMenuItems("banh duc")
        assertEquals(listOf(banhDuc), results)
    }

    @Test
    fun `search with accented query still works`() {
        val results = repository.searchMenuItems("Gà Nướng")
        assertEquals(listOf(gaNuong), results)
    }

    @Test
    fun `search matches by description and by category displayName`() {
        // "mát lạnh" nằm trong mô tả Coca Cola
        assertEquals(listOf(coca), repository.searchMenuItems("mat lanh"))
        // "Nước uống" là displayName của FoodCategory.BEVERAGE
        assertEquals(listOf(coca), repository.searchMenuItems("nuoc uong"))
    }

    @Test
    fun `blank query returns all items and unmatched query returns empty`() {
        assertEquals(4, repository.searchMenuItems("   ").size)
        assertTrue(repository.searchMenuItems("pizza").isEmpty())
    }

    @Test
    fun `getMenuItemsByCategory filters by category`() {
        val mainDishes = repository.getMenuItemsByCategory(FoodCategory.MAIN_DISH)
        assertEquals(setOf(1, 2), mainDishes.map { it.id }.toSet())
    }

    @Test
    fun `getMenuItemById returns null for unknown id`() {
        assertNull(repository.getMenuItemById(99))
    }

    @Test
    fun `saveMenuItem assigns incremental id when id is not positive`() {
        val saved = repository.saveMenuItem(
            MenuItem(0, "Trà Tắc", "Trà tắc giải khát", 20000, FoodCategory.BEVERAGE)
        )
        assertEquals(5, saved.id)
        assertEquals(5, repository.getAllMenuItems().size)
        assertEquals(saved, repository.getMenuItemById(5))

        val savedAgain = repository.saveMenuItem(
            MenuItem(0, "Khác", "Mô tả", 10000, FoodCategory.SIDE_DISH)
        )
        assertEquals(6, savedAgain.id)
    }

    @Test
    fun `saveMenuItem with existing id updates in place`() {
        val updated = repository.saveMenuItem(gaNuong.copy(price = 999000))
        assertEquals(1, updated.id)
        assertEquals("Không thêm phần tử mới khi cập nhật", 4, repository.getAllMenuItems().size)
        assertEquals(999000L, repository.getMenuItemById(1)!!.price)
    }

    @Test
    fun `deleteMenuItem returns true only for an existing id`() {
        assertTrue(repository.deleteMenuItem(2))
        assertFalse(repository.deleteMenuItem(2))
        assertNull(repository.getMenuItemById(2))
        assertEquals(3, repository.getAllMenuItems().size)
    }
}
