package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.UserRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthenticationServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper;
import com.example.chieuthu3_ketthucmonhoc.domain.services.UnifiedLoginResult;

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

    @Test
    public void updateProfileUpdatesCustomerInfoAndPasswordSuccessfully() {
        AuthResult<Customer> reg = authService.register("Trần Văn B", "tranb@example.com", "0944556677", "oldPass123");
        assertTrue(reg.isSuccess());
        int id = reg.getData().getId();

        AuthResult<Customer> updated = authService.updateProfile(
                id,
                "Trần Văn B (VIP)",
                "tranb.vip@example.com",
                "Bàn 08 - Tầng 2",
                "newPass456"
        );
        assertTrue(updated.isSuccess());
        assertEquals("Trần Văn B (VIP)", updated.getData().getFullName());
        assertEquals("tranb.vip@example.com", updated.getData().getEmail());
        assertEquals("Bàn 08 - Tầng 2", updated.getData().getAddress());
        assertTrue(SecurityHelper.verifyPassword("newPass456", updated.getData().getPasswordHash()));
        assertEquals("Trần Văn B (VIP)", authService.getCurrentCustomer().getFullName());
    }

    @Test
    public void searchCustomersFiltersByNamePhoneAndEmail() {
        authService.register("Lê Thị Hoa", "hoa.le@example.com", "0901112222", "pass123");
        authService.register("Phạm Minh Tuấn", "tuan.pham@example.com", "0903334444", "pass123");

        assertEquals(2, authService.getAllCustomers().size());
        assertEquals(1, authService.searchCustomers("Hoa").size());
        assertEquals(1, authService.searchCustomers("090333").size());
        assertEquals(1, authService.searchCustomers("tuan.pham").size());
    }

    @Test
    public void unifiedLoginRoutesCustomerByPhoneOrEmail() {
        UserRepositoryImpl seededRepo = new UserRepositoryImpl();
        AuthenticationServiceImpl seededAuth = new AuthenticationServiceImpl(seededRepo);

        AuthResult<UnifiedLoginResult> byPhone = seededAuth.login("0901234567", "123456");
        assertTrue(byPhone.isSuccess());
        assertNotNull(byPhone.getData());
        assertFalse(byPhone.getData().isAdmin());
        assertNotNull(byPhone.getData().getCustomer());
        assertNotNull(seededAuth.getCurrentCustomer());
        assertNull(seededAuth.getCurrentAdmin());

        seededAuth.logout();

        AuthResult<UnifiedLoginResult> byEmail = seededAuth.login("khachhang@restaurant.com", "123456");
        assertTrue(byEmail.isSuccess());
        assertFalse(byEmail.getData().isAdmin());
        assertNotNull(seededAuth.getCurrentCustomer());
    }

    @Test
    public void unifiedLoginRoutesAdminByUsernameOrPhoneAndClearsCustomerSession() {
        UserRepositoryImpl seededRepo = new UserRepositoryImpl();
        AuthenticationServiceImpl seededAuth = new AuthenticationServiceImpl(seededRepo);

        // Đăng nhập khách trước, sau đó dùng chung form chuyển sang admin
        AuthResult<UnifiedLoginResult> customerFirst = seededAuth.login("0901234567", "123456");
        assertTrue(customerFirst.isSuccess());
        assertNotNull(seededAuth.getCurrentCustomer());

        AuthResult<UnifiedLoginResult> adminByUsername = seededAuth.login("admin", "admin123");
        assertTrue(adminByUsername.isSuccess());
        assertTrue(adminByUsername.getData().isAdmin());
        assertNotNull(adminByUsername.getData().getAdminAccount());
        assertNotNull(seededAuth.getCurrentAdmin());
        assertNull(seededAuth.getCurrentCustomer());

        seededAuth.logout();

        AuthResult<UnifiedLoginResult> adminByPhone = seededAuth.login("0900000000", "admin123");
        assertTrue(adminByPhone.isSuccess());
        assertTrue(adminByPhone.getData().isAdmin());
        assertNotNull(seededAuth.getCurrentAdmin());
    }

    @Test
    public void unifiedLoginRejectsInvalidCredentialsWithoutStartingSession() {
        UserRepositoryImpl seededRepo = new UserRepositoryImpl();
        AuthenticationServiceImpl seededAuth = new AuthenticationServiceImpl(seededRepo);

        AuthResult<UnifiedLoginResult> wrongPass = seededAuth.login("admin", "sai-mat-khau");
        assertFalse(wrongPass.isSuccess());
        assertEquals("Mật khẩu không chính xác", wrongPass.getMessage());
        assertNull(seededAuth.getCurrentAdmin());
        assertNull(seededAuth.getCurrentCustomer());

        AuthResult<UnifiedLoginResult> unknown = seededAuth.login("khongtontai", "123456");
        assertFalse(unknown.isSuccess());
        assertEquals("Tài khoản không tồn tại trên hệ thống", unknown.getMessage());
    }
}
