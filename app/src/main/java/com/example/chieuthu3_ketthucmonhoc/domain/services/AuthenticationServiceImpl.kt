package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer

class AuthenticationServiceImpl(
    private val userRepository: IUserRepository
) : IAuthenticationService {

    private var currentCustomer: Customer? = null
    private var currentAdmin: AdminAccount? = null

    override fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): AuthResult<Customer> {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phoneNumber.trim()

        if (trimmedName.isBlank()) {
            return AuthResult.Error("Vui lòng nhập họ và tên")
        }
        if (trimmedPhone.isBlank() || trimmedPhone.length < 9) {
            return AuthResult.Error("Số điện thoại không hợp lệ (tối thiểu 9 chữ số)")
        }
        if (password.length < 6) {
            return AuthResult.Error("Mật khẩu phải chứa ít nhất 6 ký tự")
        }
        if (trimmedEmail.isNotBlank() && !trimmedEmail.contains("@")) {
            return AuthResult.Error("Địa chỉ email không đúng định dạng")
        }

        if (userRepository.getCustomerByPhone(trimmedPhone) != null) {
            return AuthResult.Error("Số điện thoại đã được đăng ký trong hệ thống")
        }
        if (trimmedEmail.isNotBlank() && userRepository.getCustomerByEmail(trimmedEmail) != null) {
            return AuthResult.Error("Email đã được đăng ký trong hệ thống")
        }

        val newCustomer = Customer(
            id = 0,
            fullName = trimmedName,
            email = trimmedEmail,
            phoneNumber = trimmedPhone,
            passwordHash = SecurityHelper.hashPassword(password)
        )
        val saved = userRepository.saveCustomer(newCustomer)
        currentCustomer = saved
        return AuthResult.Success(saved)
    }

    override fun loginCustomer(phoneOrEmail: String, password: String): AuthResult<Customer> {
        val query = phoneOrEmail.trim()
        if (query.isBlank() || password.isBlank()) {
            return AuthResult.Error("Vui lòng nhập đầy đủ tài khoản và mật khẩu")
        }

        val customer = if (query.contains("@")) {
            userRepository.getCustomerByEmail(query)
        } else {
            userRepository.getCustomerByPhone(query)
        }

        if (customer == null) {
            return AuthResult.Error("Tài khoản không tồn tại trên hệ thống")
        }

        if (!SecurityHelper.verifyPassword(password, customer.passwordHash)) {
            return AuthResult.Error("Mật khẩu không chính xác")
        }

        currentCustomer = customer
        return AuthResult.Success(customer)
    }

    override fun loginAdmin(username: String, password: String): AuthResult<AdminAccount> {
        val trimmedUser = username.trim()
        if (trimmedUser.isBlank() || password.isBlank()) {
            return AuthResult.Error("Vui lòng nhập tài khoản và mật khẩu quản trị")
        }

        val admin = userRepository.getAdminByUsername(trimmedUser)
            ?: return AuthResult.Error("Tài khoản quản trị không tồn tại")

        if (!SecurityHelper.verifyPassword(password, admin.passwordHash)) {
            return AuthResult.Error("Mật khẩu quản trị không chính xác")
        }

        currentAdmin = admin
        return AuthResult.Success(admin)
    }

    override fun logout() {
        currentCustomer = null
        currentAdmin = null
    }

    override fun getCurrentCustomer(): Customer? = currentCustomer

    override fun getCurrentAdmin(): AdminAccount? = currentAdmin
}
