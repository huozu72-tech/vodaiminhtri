package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Khóa các bất biến của seed data trong InitialDataFactory (trước đây chưa có test):
 * id/mã đơn không trùng, giá dương, đơn seed phải trỏ tới món có thật trong thực đơn,
 * mọi danh mục đều có ít nhất một món, và mật khẩu seed luôn được băm
 * (không bao giờ lưu plaintext).
 */
class InitialDataFactoryTest {

    private val knownPlaintextPasswords = listOf("123456", "admin123", "manager123")

    private lateinit var menuItems: List<MenuItem>
    private lateinit var orders: List<Order>

    @Before
    fun setup() {
        menuItems = InitialDataFactory.createInitialMenuItems()
        orders = InitialDataFactory.createInitialOrders(menuItems)
    }

    @Test
    fun `menu items have unique positive ids with positive prices and non-blank text`() {
        assertTrue("Thực đơn seed không được rỗng", menuItems.isNotEmpty())

        val ids = menuItems.map { it.id }
        assertEquals("Id món không được trùng", ids.size, ids.distinct().size)
        assertTrue(ids.all { it > 0 })
        assertTrue(menuItems.all { it.price > 0 })
        assertTrue(menuItems.all { it.name.isNotBlank() })
        assertTrue(menuItems.all { it.description.isNotBlank() })
    }

    @Test
    fun `menu covers every food category`() {
        FoodCategory.entries.forEach { category ->
            assertTrue(
                "Danh mục '${category.displayName}' phải có ít nhất một món seed",
                menuItems.any { it.category == category }
            )
        }
    }

    @Test
    fun `customers have unique phones and emails with hashed passwords`() {
        val customers = InitialDataFactory.createInitialCustomers()
        assertTrue(customers.isNotEmpty())

        assertEquals(
            "Số điện thoại khách không được trùng",
            customers.size,
            customers.map { it.phoneNumber }.distinct().size
        )
        assertEquals(
            "Email khách không được trùng (kể cả khác hoa/thường)",
            customers.size,
            customers.map { it.email.lowercase() }.distinct().size
        )
        customers.forEach { customer ->
            assertTrue(customer.passwordHash.isNotBlank())
            assertFalse(
                "Mật khẩu seed phải được băm, không lưu plaintext",
                knownPlaintextPasswords.contains(customer.passwordHash)
            )
        }
    }

    @Test
    fun `admins have unique usernames with hashed passwords`() {
        val admins = InitialDataFactory.createInitialAdmins()
        assertTrue(admins.isNotEmpty())

        assertEquals(
            "Username quản trị không được trùng (kể cả khác hoa/thường)",
            admins.size,
            admins.map { it.username.lowercase() }.distinct().size
        )
        admins.forEach { admin ->
            assertTrue(admin.passwordHash.isNotBlank())
            assertFalse(
                "Mật khẩu quản trị phải được băm, không lưu plaintext",
                knownPlaintextPasswords.contains(admin.passwordHash)
            )
        }
    }

    @Test
    fun `orders have unique codes with valid shape and reference existing menu items`() {
        assertTrue("Danh sách đơn seed không được rỗng", orders.isNotEmpty())
        val menuIds = menuItems.map { it.id }.toSet()

        orders.forEach { order ->
            assertTrue(
                "Mã đơn sai định dạng #ORD-<số>: ${order.orderId}",
                Regex("#ORD-\\d+").matches(order.orderId)
            )
            assertTrue(order.tableNumber.isNotBlank())
            assertTrue(order.items.isNotEmpty())
            order.items.forEach { item ->
                assertTrue(
                    "Đơn ${order.orderId} trỏ tới món không có trong thực đơn seed: id=${item.menuItem.id}",
                    menuIds.contains(item.menuItem.id)
                )
            }
        }

        assertEquals(
            "Mã đơn seed không được trùng",
            orders.size,
            orders.map { it.orderId }.distinct().size
        )
    }

    @Test
    fun `order total equals the sum of its item subtotals`() {
        orders.forEach { order ->
            assertEquals(
                "Tổng tiền đơn ${order.orderId} phải bằng tổng subtotal các món",
                order.items.sumOf { it.subtotal },
                order.totalAmount
            )
        }
    }
}
