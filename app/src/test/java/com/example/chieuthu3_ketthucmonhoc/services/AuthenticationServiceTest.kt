package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.UserRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthenticationServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthenticationServiceTest {

    private lateinit var authService: AuthenticationServiceImpl
    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setup() {
        userRepository = UserRepositoryImpl(
            initialCustomers = emptyList(),
            initialAdmins = emptyList()
        )
        authService = AuthenticationServiceImpl(userRepository)
    }

    @Test
    fun `register successfully with valid input`() {
        val result = authService.register(
            fullName = "Người Dùng Thử Nghiệm",
            email = "user.test@example.com",
            phoneNumber = "0988776655",
            password = "securePassword123"
        )

        assertTrue(result is AuthResult.Success)
        val customer = (result as AuthResult.Success<Customer>).data
        assertEquals("Người Dùng Thử Nghiệm", customer.fullName)
        assertEquals("user.test@example.com", customer.email)
        assertEquals("0988776655", customer.phoneNumber)
        assertTrue(SecurityHelper.verifyPassword("securePassword123", customer.passwordHash))
        assertEquals(customer, authService.getCurrentCustomer())
    }

    @Test
    fun `register fails when phone number is duplicated`() {
        authService.register("User One", "user1@example.com", "0911223344", "pass123")
        val duplicateResult = authService.register("User Two", "user2@example.com", "0911223344", "pass456")

        assertTrue(duplicateResult is AuthResult.Error)
        assertEquals("Số điện thoại đã được đăng ký trong hệ thống", (duplicateResult as AuthResult.Error).message)
    }

    @Test
    fun `register fails with short password`() {
        val result = authService.register("User Short", "short@example.com", "0911223355", "123")
        assertTrue(result is AuthResult.Error)
        assertEquals("Mật khẩu phải chứa ít nhất 6 ký tự", (result as AuthResult.Error).message)
    }

    @Test
    fun `login customer succeeds with correct credentials`() {
        authService.register("Khách Hàng Mới", "newcustomer@example.com", "0933445566", "password123")
        authService.logout()
        assertNull(authService.getCurrentCustomer())

        val loginResult = authService.loginCustomer("0933445566", "password123")
        assertTrue(loginResult is AuthResult.Success)
        assertEquals("Khách Hàng Mới", (loginResult as AuthResult.Success<Customer>).data.fullName)
    }

    @Test
    fun `login customer fails with wrong password`() {
        authService.register("Khách Hàng", "client@example.com", "0933445577", "password123")
        val result = authService.loginCustomer("0933445577", "wrongPassword")
        assertTrue(result is AuthResult.Error)
        assertEquals("Mật khẩu không chính xác", (result as AuthResult.Error).message)
    }

    @Test
    fun `admin login with seeded data`() {
        val seededRepo = UserRepositoryImpl()
        val seededAuth = AuthenticationServiceImpl(seededRepo)

        val result = seededAuth.loginAdmin("admin", "admin123")
        assertTrue(result is AuthResult.Success)
        assertNotNull(seededAuth.getCurrentAdmin())
    }
}
