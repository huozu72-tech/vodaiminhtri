package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.UserRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthenticationServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthenticationServiceTest {

    private AuthenticationServiceImpl authService;
    private UserRepositoryImpl userRepository;

    @Before
    public void setup() {
        userRepository = new UserRepositoryImpl(
                Collections.emptyList(),
                Collections.emptyList()
        );
        authService = new AuthenticationServiceImpl(userRepository);
    }

    @Test
    public void registerSuccessfullyWithValidInput() {
        AuthResult<Customer> result = authService.register(
                "Người Dùng Thử Nghiệm",
                "user.test@example.com",
                "0988776655",
                "securePassword123"
        );

        assertTrue(result.isSuccess());
        Customer customer = result.getData();
        assertNotNull(customer);
        assertEquals("Người Dùng Thử Nghiệm", customer.getFullName());
        assertEquals("user.test@example.com", customer.getEmail());
        assertEquals("0988776655", customer.getPhoneNumber());
        assertTrue(SecurityHelper.verifyPassword("securePassword123", customer.getPasswordHash()));
        assertEquals(customer, authService.getCurrentCustomer());
    }

    @Test
    public void registerFailsWhenPhoneNumberIsDuplicated() {
        authService.register("User One", "user1@example.com", "0911223344", "pass123");
        AuthResult<Customer> duplicateResult = authService.register("User Two", "user2@example.com", "0911223344", "pass456");

        assertFalse(duplicateResult.isSuccess());
        assertEquals("Số điện thoại đã được đăng ký trong hệ thống", duplicateResult.getMessage());
    }

    @Test
    public void registerFailsWithShortPassword() {
        AuthResult<Customer> result = authService.register("User Short", "short@example.com", "0911223355", "123");
        assertFalse(result.isSuccess());
        assertEquals("Mật khẩu phải chứa ít nhất 6 ký tự", result.getMessage());
    }

    @Test
    public void loginCustomerSucceedsWithCorrectCredentials() {
        authService.register("Khách Hàng Mới", "newcustomer@example.com", "0933445566", "password123");
        authService.logout();
        assertNull(authService.getCurrentCustomer());

        AuthResult<Customer> loginResult = authService.loginCustomer("0933445566", "password123");
        assertTrue(loginResult.isSuccess());
        assertEquals("Khách Hàng Mới", loginResult.getData().getFullName());
    }

    @Test
    public void loginCustomerFailsWithWrongPassword() {
        authService.register("Khách Hàng", "client@example.com", "0933445577", "password123");
        AuthResult<Customer> result = authService.loginCustomer("0933445577", "wrongPassword");
        assertFalse(result.isSuccess());
        assertEquals("Mật khẩu không chính xác", result.getMessage());
    }

    @Test
    public void adminLoginWithSeededData() {
        UserRepositoryImpl seededRepo = new UserRepositoryImpl();
        AuthenticationServiceImpl seededAuth = new AuthenticationServiceImpl(seededRepo);

        AuthResult<AdminAccount> result = seededAuth.loginAdmin("admin", "admin123");
        assertTrue(result.isSuccess());
        assertNotNull(seededAuth.getCurrentAdmin());
    }
}
