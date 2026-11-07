package com.example.chieuthu3_ketthucmonhoc.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chieuthu3_ketthucmonhoc.data.database.entity.AdminEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.CustomerEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM customers ORDER BY id")
    List<CustomerEntity> getAllCustomers();

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    CustomerEntity getCustomerById(int id);

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    CustomerEntity getCustomerByPhone(String phone);

    @Query("SELECT * FROM customers WHERE email = :email COLLATE NOCASE LIMIT 1")
    CustomerEntity getCustomerByEmail(String email);

    @Insert
    long insertCustomer(CustomerEntity customer);

    @Insert
    void insertCustomers(List<CustomerEntity> customers);

    @Update
    void updateCustomer(CustomerEntity customer);

    @Query("SELECT COUNT(*) FROM customers")
    int countCustomers();

    @Query("SELECT * FROM admins ORDER BY id")
    List<AdminEntity> getAllAdmins();

    @Query("SELECT * FROM admins WHERE username = :username COLLATE NOCASE LIMIT 1")
    AdminEntity getAdminByUsername(String username);

    @Insert
    void insertAdmins(List<AdminEntity> admins);

    @Query("SELECT COUNT(*) FROM admins")
    int countAdmins();
}
