package com.example.chieuthu3_ketthucmonhoc.data.repository

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer
import java.util.concurrent.CopyOnWriteArrayList

class UserRepositoryImpl(
    initialCustomers: List<Customer> = InitialDataFactory.createInitialCustomers(),
    initialAdmins: List<AdminAccount> = InitialDataFactory.createInitialAdmins()
) : IUserRepository {

    private val customers = CopyOnWriteArrayList<Customer>(initialCustomers)
    private val admins = CopyOnWriteArrayList<AdminAccount>(initialAdmins)
    private var nextCustomerId = (customers.maxOfOrNull { it.id } ?: 0) + 1

    override fun getCustomerById(id: Int): Customer? {
        return customers.find { it.id == id }
    }

    override fun getCustomerByPhone(phone: String): Customer? {
        return customers.find { it.phoneNumber == phone.trim() }
    }

    override fun getCustomerByEmail(email: String): Customer? {
        return customers.find { it.email.equals(email.trim(), ignoreCase = true) }
    }

    override fun saveCustomer(customer: Customer): Customer {
        val target = if (customer.id <= 0) {
            customer.copy(id = nextCustomerId++)
        } else {
            customer
        }
        val existingIndex = customers.indexOfFirst { it.id == target.id }
        if (existingIndex >= 0) {
            customers[existingIndex] = target
        } else {
            customers.add(target)
        }
        return target
    }

    override fun getAllCustomers(): List<Customer> = customers.toList()

    override fun getAdminByUsername(username: String): AdminAccount? {
        return admins.find { it.username.equals(username.trim(), ignoreCase = true) }
    }

    override fun getAllAdmins(): List<AdminAccount> = admins.toList()
}
