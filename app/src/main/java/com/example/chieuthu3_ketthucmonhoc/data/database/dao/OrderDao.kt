package com.example.chieuthu3_ketthucmonhoc.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderItemEntity

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAll(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE order_id = :orderId COLLATE NOCASE LIMIT 1")
    fun getById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun getByCustomer(customerId: Int): List<OrderEntity>

    @Insert
    fun insert(order: OrderEntity)

    @Update
    fun update(order: OrderEntity)

    @Query("DELETE FROM orders WHERE order_id = :orderId")
    fun deleteById(orderId: String): Int

    @Query("UPDATE orders SET status = :status WHERE order_id = :orderId")
    fun updateStatus(orderId: String, status: String): Int

    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY id")
    fun getItems(orderId: String): List<OrderItemEntity>

    @Insert
    fun insertItems(items: List<OrderItemEntity>)

    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    fun deleteItems(orderId: String)

    @Query("SELECT COUNT(*) FROM orders")
    fun count(): Int
}
