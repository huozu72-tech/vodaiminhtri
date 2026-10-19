package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer

interface IUserRepository {
    fun getCustomerById(id: Int): Customer?
    fun getCustomerByPhone(phone: String): Customer?
    fun getCustomerByEmail(email: String): Customer?
    fun saveCustomer(customer: Customer): Customer
    fun getAllCustomers(): List<Customer>

    fun getAdminByUsername(username: String): AdminAccount?
    fun getAllAdmins(): List<AdminAccount>
}
