package com.example.chieuthu3_ketthucmonhoc.data.database

import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toDomain
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toEntity
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus

/**
 * Repository đơn hàng trên SQLite (Room).
 * Upsert theo mã đơn không phân biệt hoa/thường (giữ nguyên ngữ nghĩa bản in-memory);
 * chi tiết đơn thay thế toàn bộ trong cùng transaction, CASCADE lo phần dọn dẹp.
 */
class RoomOrderRepository(
    private val database: AppDatabase
) : IOrderRepository {

    private val dao = database.orderDao()
    private val menuDao = database.menuItemDao()

    override fun getAllOrders(): List<Order> = dao.getAll().map(::assembleOrder)

    override fun getOrderById(orderId: String): Order? =
        dao.getById(orderId.trim())?.let(::assembleOrder)

    override fun getOrdersByCustomer(customerId: Int): List<Order> =
        dao.getByCustomer(customerId).map(::assembleOrder)

    override fun saveOrder(order: Order): Order {
        val entity = order.toEntity()
        database.runInTransaction {
            val existing = dao.getById(entity.orderId)
            when {
                existing == null -> dao.insert(entity)
                existing.orderId == entity.orderId -> dao.update(entity)
                else -> {
                    // Trùng mã khác hoa/thường: gộp về một dòng, giữ mã mới
                    dao.deleteById(existing.orderId)
                    dao.insert(entity)
                }
            }
            dao.deleteItems(entity.orderId)
            dao.insertItems(order.items.map { it.toEntity(entity.orderId) })
        }
        return order
    }

    override fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        return dao.updateStatus(orderId.trim(), newStatus.name) > 0
    }

    private fun assembleOrder(entity: com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity): Order {
        val items = dao.getItems(entity.orderId).mapNotNull { itemEntity ->
            menuDao.getById(itemEntity.menuItemId)?.let { menuEntity ->
                itemEntity.toDomain(menuEntity.toDomain())
            }
        }
        return entity.toDomain().copy(items = items)
    }
}
