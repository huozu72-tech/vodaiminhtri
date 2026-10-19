package com.example.chieuthu3_ketthucmonhoc.data.datasource

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute
import com.example.chieuthu3_ketthucmonhoc.domain.models.UserRole
import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper

object InitialDataFactory {

    fun createInitialAdmins(): List<AdminAccount> {
        return listOf(
            AdminAccount(
                id = 1,
                username = "admin",
                passwordHash = SecurityHelper.hashPassword("admin123"),
                fullName = "Quản Trị Viên",
                role = UserRole.SUPER_ADMIN
            ),
            AdminAccount(
                id = 2,
                username = "manager",
                passwordHash = SecurityHelper.hashPassword("manager123"),
                fullName = "Quản Lý Nhà Hàng",
                role = UserRole.MANAGER
            )
        )
    }

    fun createInitialCustomers(): List<Customer> {
        return listOf(
            Customer(
                id = 1,
                fullName = "Khách Hàng Thân Thiết",
                email = "khachhang@restaurant.com",
                phoneNumber = "0901234567",
                passwordHash = SecurityHelper.hashPassword("123456"),
                address = "Bàn 12 - Khu Vực A"
            )
        )
    }

    fun createInitialMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                id = 1,
                name = "Chân Giò Muối Chiên Giòn",
                description = "Chân Giò Muối Chiên Giòn da giòn rụm, thịt mềm thơm đậm đà hương vị",
                price = 120000,
                category = FoodCategory.MAIN_DISH,
                tasteAttributes = listOf(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                rating = 4.9,
                orderCount = 45
            ),
            MenuItem(
                id = 2,
                name = "Ếch Nướng Muối Ớt Lá Chanh",
                description = "Ếch đồng tươi nướng muối ớt cay nồng thơm lừng hương lá chanh",
                price = 85000,
                category = FoodCategory.MAIN_DISH,
                tasteAttributes = listOf(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                rating = 4.7,
                orderCount = 38
            ),
            MenuItem(
                id = 3,
                name = "Gà Nướng Mật Ong",
                description = "Gà ta ướp mật ong hoa rừng nướng vàng óng, thịt ngọt thơm đậm vị",
                price = 150000,
                category = FoodCategory.MAIN_DISH,
                tasteAttributes = listOf(TasteAttribute.SWEET, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                rating = 4.9,
                orderCount = 62
            ),
            MenuItem(
                id = 4,
                name = "Gà Quay Bánh Bao",
                description = "Gà quay thơm lừng ăn kèm bánh bao chiên vàng giòn rụm",
                price = 160000,
                category = FoodCategory.MAIN_DISH,
                tasteAttributes = listOf(TasteAttribute.SAVORY, TasteAttribute.CRISPY),
                rating = 4.8,
                orderCount = 50
            ),
            MenuItem(
                id = 5,
                name = "Tôm Chiên Xù",
                description = "Tôm sú tươi tẩm bột chiên xù vàng ruộm, chấm kèm sốt tương ớt mayonnaise",
                price = 95000,
                category = FoodCategory.MAIN_DISH,
                tasteAttributes = listOf(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                rating = 4.7,
                orderCount = 41
            ),
            MenuItem(
                id = 6,
                name = "Tôm Nướng Muối Ớt",
                description = "Tôm sú nướng muối ớt cay xè, giữ trọn vị ngọt tự nhiên của tôm biển",
                price = 105000,
                category = FoodCategory.MAIN_DISH,
                tasteAttributes = listOf(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                rating = 4.8,
                orderCount = 53
            ),
            MenuItem(
                id = 7,
                name = "Chân Gà Sả Tắc",
                description = "Chân gà rút xương ngâm sả tắc chua cay thanh mát giòn sần sật",
                price = 60000,
                category = FoodCategory.SIDE_DISH,
                tasteAttributes = listOf(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.REFRESHING, TasteAttribute.CRISPY),
                rating = 4.8,
                orderCount = 70
            ),
            MenuItem(
                id = 8,
                name = "Chân Gà Sốt Thái",
                description = "Chân gà ngập sốt Thái chua cay đậm vị thơm mùi xoài non và cóc non",
                price = 65000,
                category = FoodCategory.SIDE_DISH,
                tasteAttributes = listOf(TasteAttribute.SPICY, TasteAttribute.SOUR, TasteAttribute.SWEET),
                rating = 4.9,
                orderCount = 82
            ),
            MenuItem(
                id = 9,
                name = "Gỏi Ngó Sen",
                description = "Gỏi ngó sen tôm thịt thanh đạm giải ngấy, chua ngọt vừa miệng",
                price = 55000,
                category = FoodCategory.SIDE_DISH,
                tasteAttributes = listOf(TasteAttribute.SOUR, TasteAttribute.SWEET, TasteAttribute.REFRESHING, TasteAttribute.CRISPY),
                rating = 4.6,
                orderCount = 33
            ),
            MenuItem(
                id = 10,
                name = "Gỏi Xoài Tôm Khô",
                description = "Xoài xanh băm sợi trộn tôm khô thơm bùi nước mắm chua cay hấp dẫn",
                price = 70000,
                category = FoodCategory.SIDE_DISH,
                tasteAttributes = listOf(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.CRISPY),
                rating = 4.7,
                orderCount = 29
            ),
            MenuItem(
                id = 11,
                name = "Gỏi Xoài Tôm Thịt",
                description = "Gỏi xoài tôm thịt đầy đặn, rắc đậu phộng rang giòn thơm nức mũi",
                price = 80000,
                category = FoodCategory.SIDE_DISH,
                tasteAttributes = listOf(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.SAVORY),
                rating = 4.8,
                orderCount = 44
            ),
            MenuItem(
                id = 12,
                name = "7Up",
                description = "Nước giải khát 7Up có ga hương chanh thanh mát sảng khoái",
                price = 15000,
                category = FoodCategory.BEVERAGE,
                tasteAttributes = listOf(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                rating = 4.9,
                orderCount = 110
            ),
            MenuItem(
                id = 13,
                name = "Coca Cola",
                description = "Nước ngọt có ga Coca Cola mát lạnh giải nhiệt tức thì",
                price = 15000,
                category = FoodCategory.BEVERAGE,
                tasteAttributes = listOf(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                rating = 4.9,
                orderCount = 135
            ),
            MenuItem(
                id = 14,
                name = "Pepsi",
                description = "Nước giải khát Pepsi mát lạnh sảng khoái kích thích vị giác",
                price = 15000,
                category = FoodCategory.BEVERAGE,
                tasteAttributes = listOf(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                rating = 4.8,
                orderCount = 98
            ),
            MenuItem(
                id = 15,
                name = "Sting",
                description = "Nước tăng lực Sting dâu thơm ngon tiếp thêm năng lượng",
                price = 15000,
                category = FoodCategory.BEVERAGE,
                tasteAttributes = listOf(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                rating = 4.8,
                orderCount = 88
            )
        )
    }

    fun createInitialOrders(menuItems: List<MenuItem>): List<Order> {
        val findItem = { id: Int -> menuItems.find { it.id == id } ?: menuItems.first() }
        return listOf(
            Order(
                orderId = "#ORD-1024",
                tableNumber = "Bàn 12",
                customerId = 1,
                items = listOf(
                    OrderItem(1, findItem(1), 1),
                    OrderItem(2, findItem(12), 2)
                ),
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis() - 3600000
            ),
            Order(
                orderId = "#ORD-1025",
                tableNumber = "Bàn 05",
                customerId = 1,
                items = listOf(
                    OrderItem(3, findItem(3), 1),
                    OrderItem(4, findItem(13), 1)
                ),
                status = OrderStatus.PREPARING,
                createdAt = System.currentTimeMillis() - 1800000
            ),
            Order(
                orderId = "#ORD-1023",
                tableNumber = "Bàn 02",
                customerId = 1,
                items = listOf(
                    OrderItem(5, findItem(5), 1),
                    OrderItem(6, findItem(15), 2)
                ),
                status = OrderStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - 7200000
            )
        )
    }
}
