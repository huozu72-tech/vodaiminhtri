package com.example.chieuthu3_ketthucmonhoc.data.database

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toEntity

/**
 * Gieo dữ liệu ban đầu ("dữ liệu ảo") vào SQLite lần đầu mở app.
 * Dữ liệu mẫu lấy từ InitialDataFactory và phải thỏa mọi ràng buộc schema:
 * đơn seed chỉ trỏ customer/món có thật, mỗi món một dòng mỗi đơn, mã đơn duy nhất.
 * Chạy trong một transaction nên nếu vi phạm FK/UNIQUE sẽ rollback toàn bộ.
 */
object DatabaseSeeder {

    fun seedIfEmpty(db: AppDatabase) {
        val alreadySeeded = db.menuItemDao().count() > 0 ||
            db.userDao().countCustomers() > 0 ||
            db.orderDao().count() > 0
        if (alreadySeeded) return

        db.runInTransaction {
            val menuItems = InitialDataFactory.createInitialMenuItems()
            db.menuItemDao().insertAll(menuItems.map { it.toEntity() })

            db.userDao().insertCustomers(InitialDataFactory.createInitialCustomers().map { it.toEntity() })
            db.userDao().insertAdmins(InitialDataFactory.createInitialAdmins().map { it.toEntity() })

            InitialDataFactory.createInitialOrders(menuItems).forEach { order ->
                val entity = order.toEntity()
                db.orderDao().insert(entity)
                db.orderDao().insertItems(order.items.map { it.toEntity(entity.orderId) })
            }
        }
    }
}
