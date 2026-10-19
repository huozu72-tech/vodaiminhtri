package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

interface IAuthenticationService {
    fun register(fullName: String, email: String, phoneNumber: String, password: String): AuthResult<Customer>
    fun loginCustomer(phoneOrEmail: String, password: String): AuthResult<Customer>
    fun loginAdmin(username: String, password: String): AuthResult<AdminAccount>
    fun logout()
    fun getCurrentCustomer(): Customer?
    fun getCurrentAdmin(): AdminAccount?
}
