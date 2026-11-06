package com.example.chieuthu3_ketthucmonhoc.data.database

import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toDomain
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.toEntity
import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer

/** Repository khách hàng/quản trị trên SQLite (Room). UNIQUE phone/email/username do SQLite ép buộc. */
class RoomUserRepository(
    private val database: AppDatabase
) : IUserRepository {

    private val dao = database.userDao()

    override fun getCustomerById(id: Int): Customer? = dao.getCustomerById(id)?.toDomain()

    override fun getCustomerByPhone(phone: String): Customer? =
        dao.getCustomerByPhone(phone.trim())?.toDomain()

    override fun getCustomerByEmail(email: String): Customer? =
        dao.getCustomerByEmail(email.trim())?.toDomain()

    override fun saveCustomer(customer: Customer): Customer {
        val entity = customer.toEntity()
        if (entity.id <= 0) {
            val newId = dao.insertCustomer(entity.copy(id = 0))
            return customer.copy(id = newId.toInt())
        }
        val existing = dao.getCustomerById(entity.id)
        return if (existing == null) {
            dao.insertCustomer(entity)
            customer
        } else {
            dao.updateCustomer(entity)
            customer
        }
    }

    override fun getAllCustomers(): List<Customer> = dao.getAllCustomers().map { it.toDomain() }

    override fun getAdminByUsername(username: String): AdminAccount? =
        dao.getAdminByUsername(username.trim())?.toDomain()

    override fun getAllAdmins(): List<AdminAccount> = dao.getAllAdmins().map { it.toDomain() }
}
