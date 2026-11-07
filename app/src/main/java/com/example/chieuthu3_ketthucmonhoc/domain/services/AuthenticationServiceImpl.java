package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

public class AuthenticationServiceImpl implements IAuthenticationService {

    private final IUserRepository userRepository;
    private Customer currentCustomer;
    private AdminAccount currentAdmin;

    public AuthenticationServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthResult<Customer> register(String fullName, String email, String phoneNumber, String password) {
        String trimmedName = fullName != null ? fullName.trim() : "";
        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedPhone = phoneNumber != null ? phoneNumber.trim() : "";

        if (trimmedName.isEmpty()) {
            return AuthResult.error("Vui lòng nhập họ và tên");
        }
        if (trimmedPhone.isEmpty() || trimmedPhone.length() < 9) {
            return AuthResult.error("Số điện thoại không hợp lệ (tối thiểu 9 chữ số)");
        }
        if (password == null || password.length() < 6) {
            return AuthResult.error("Mật khẩu phải chứa ít nhất 6 ký tự");
        }
        if (!trimmedEmail.isEmpty() && !trimmedEmail.contains("@")) {
            return AuthResult.error("Địa chỉ email không đúng định dạng");
        }

        if (userRepository.getCustomerByPhone(trimmedPhone) != null) {
            return AuthResult.error("Số điện thoại đã được đăng ký trong hệ thống");
        }
        if (!trimmedEmail.isEmpty() && userRepository.getCustomerByEmail(trimmedEmail) != null) {
            return AuthResult.error("Email đã được đăng ký trong hệ thống");
        }

        Customer newCustomer = new Customer(
                0,
                trimmedName,
                trimmedEmail,
                trimmedPhone,
                SecurityHelper.hashPassword(password)
        );
        Customer saved = userRepository.saveCustomer(newCustomer);
        this.currentCustomer = saved;
        return AuthResult.success(saved);
    }

    @Override
    public AuthResult<Customer> loginCustomer(String phoneOrEmail, String password) {
        String query = phoneOrEmail != null ? phoneOrEmail.trim() : "";
        if (query.isEmpty() || password == null || password.isEmpty()) {
            return AuthResult.error("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
        }

        Customer customer = query.contains("@")
                ? userRepository.getCustomerByEmail(query)
                : userRepository.getCustomerByPhone(query);

        if (customer == null) {
            return AuthResult.error("Tài khoản không tồn tại trên hệ thống");
        }

        if (!SecurityHelper.verifyPassword(password, customer.getPasswordHash())) {
            return AuthResult.error("Mật khẩu không chính xác");
        }

        this.currentCustomer = customer;
        return AuthResult.success(customer);
    }

    @Override
    public AuthResult<AdminAccount> loginAdmin(String username, String password) {
        String trimmedUser = username != null ? username.trim() : "";
        if (trimmedUser.isEmpty() || password == null || password.isEmpty()) {
            return AuthResult.error("Vui lòng nhập tài khoản và mật khẩu quản trị");
        }

        AdminAccount admin = userRepository.getAdminByUsername(trimmedUser);
        if (admin == null) {
            return AuthResult.error("Tài khoản quản trị không tồn tại");
        }

        if (!SecurityHelper.verifyPassword(password, admin.getPasswordHash())) {
            return AuthResult.error("Mật khẩu quản trị không chính xác");
        }

        this.currentAdmin = admin;
        return AuthResult.success(admin);
    }

    @Override
    public void logout() {
        this.currentCustomer = null;
        this.currentAdmin = null;
    }

    @Override
    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    @Override
    public AdminAccount getCurrentAdmin() {
        return currentAdmin;
    }
}
