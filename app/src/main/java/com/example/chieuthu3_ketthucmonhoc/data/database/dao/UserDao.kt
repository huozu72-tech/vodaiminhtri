package com.example.chieuthu3_ketthucmonhoc.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.AdminEntity
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.CustomerEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM customers ORDER BY id")
    fun getAllCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Int): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE email = :email COLLATE NOCASE LIMIT 1")
    fun getCustomerByEmail(email: String): CustomerEntity?

    @Insert
    fun insertCustomer(customer: CustomerEntity): Long

    @Insert
    fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    fun updateCustomer(customer: CustomerEntity)

    @Query("SELECT COUNT(*) FROM customers")
    fun countCustomers(): Int

    @Query("SELECT * FROM admins ORDER BY id")
    fun getAllAdmins(): List<AdminEntity>

    @Query("SELECT * FROM admins WHERE username = :username COLLATE NOCASE LIMIT 1")
    fun getAdminByUsername(username: String): AdminEntity?

    @Insert
    fun insertAdmins(admins: List<AdminEntity>)

    @Query("SELECT COUNT(*) FROM admins")
    fun countAdmins(): Int
}
