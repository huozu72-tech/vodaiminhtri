package com.example.chieuthu3_ketthucmonhoc.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderItemEntity;

import java.util.List;

@Dao
public interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    List<OrderEntity> getAll();

    @Query("SELECT * FROM orders WHERE order_id = :orderId COLLATE NOCASE LIMIT 1")
    OrderEntity getById(String orderId);

    @Query("SELECT * FROM orders WHERE customer_id = :customerId ORDER BY created_at DESC")
    List<OrderEntity> getByCustomer(int customerId);

    @Insert
    void insert(OrderEntity order);

    @Update
    void update(OrderEntity order);

    @Query("DELETE FROM orders WHERE order_id = :orderId")
    int deleteById(String orderId);

    @Query("UPDATE orders SET status = :status WHERE order_id = :orderId")
    int updateStatus(String orderId, String status);

    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY id")
    List<OrderItemEntity> getItems(String orderId);

    @Insert
    void insertItems(List<OrderItemEntity> items);

    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    void deleteItems(String orderId);

    @Query("SELECT COUNT(*) FROM orders")
    int count();
}
