package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        this.currentAdmin = null;
        return AuthResult.success(saved);
    }

    @Override
    public AuthResult<UnifiedLoginResult> login(String identifier, String password) {
        String trimmedId = identifier != null ? identifier.trim() : "";
        if (trimmedId.isEmpty() || password == null || password.isEmpty()) {
            return AuthResult.error("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
        }

        AdminAccount admin = userRepository.getAdminByUsername(trimmedId);
        Customer customer = trimmedId.contains("@")
                ? userRepository.getCustomerByEmail(trimmedId)
                : userRepository.getCustomerByPhone(trimmedId);

        if (admin != null && SecurityHelper.verifyPassword(password, admin.getPasswordHash())) {
            this.currentAdmin = admin;
            this.currentCustomer = null;
            return AuthResult.success(new UnifiedLoginResult(admin));
        }

        if (customer != null && SecurityHelper.verifyPassword(password, customer.getPasswordHash())) {
            this.currentCustomer = customer;
            this.currentAdmin = null;
            return AuthResult.success(new UnifiedLoginResult(customer));
        }

        if (admin != null || customer != null) {
            return AuthResult.error("Mật khẩu không chính xác");
        }

        return AuthResult.error("Tài khoản không tồn tại trên hệ thống");
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
        this.currentAdmin = null;
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
        this.currentCustomer = null;
        return AuthResult.success(admin);
    }

    @Override
    public AuthResult<Customer> updateProfile(int customerId, String fullName, String email, String address, String newPassword) {
        Customer existing = userRepository.getCustomerById(customerId);
        if (existing == null) {
            return AuthResult.error("Không tìm thấy thông tin khách hàng");
        }

        String trimmedName = fullName != null ? fullName.trim() : "";
        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedAddress = address != null ? address.trim() : "";
        String trimmedNewPass = newPassword != null ? newPassword.trim() : "";

        if (trimmedName.isEmpty()) {
            return AuthResult.error("Họ và tên không được để trống");
        }
        if (!trimmedEmail.isEmpty() && !trimmedEmail.contains("@")) {
            return AuthResult.error("Địa chỉ email không đúng định dạng");
        }
        if (!trimmedEmail.isEmpty()) {
            Customer byEmail = userRepository.getCustomerByEmail(trimmedEmail);
            if (byEmail != null && byEmail.getId() != customerId) {
                return AuthResult.error("Email này đã được sử dụng bởi tài khoản khác");
            }
        }
        if (!trimmedNewPass.isEmpty() && trimmedNewPass.length() < 6) {
            return AuthResult.error("Mật khẩu mới phải chứa ít nhất 6 ký tự");
        }

        String passwordHash = !trimmedNewPass.isEmpty()
                ? SecurityHelper.hashPassword(trimmedNewPass)
                : existing.getPasswordHash();

        Customer updated = new Customer(
                existing.getId(),
                trimmedName,
                trimmedEmail,
                existing.getPhoneNumber(),
                passwordHash,
                trimmedAddress.isEmpty() ? null : trimmedAddress,
                existing.getCreatedAt()
        );
        Customer saved = userRepository.saveCustomer(updated);
        if (currentCustomer != null && currentCustomer.getId() == customerId) {
            currentCustomer = saved;
        }
        return AuthResult.success(saved);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return userRepository.getAllCustomers();
    }

    @Override
    public List<Customer> searchCustomers(String query) {
        List<Customer> all = userRepository.getAllCustomers();
        if (query == null || query.trim().isEmpty()) {
            return all;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<Customer> filtered = new ArrayList<>();
        for (Customer c : all) {
            boolean matchName = c.getFullName() != null && c.getFullName().toLowerCase(Locale.ROOT).contains(q);
            boolean matchPhone = c.getPhoneNumber() != null && c.getPhoneNumber().toLowerCase(Locale.ROOT).contains(q);
            boolean matchEmail = c.getEmail() != null && c.getEmail().toLowerCase(Locale.ROOT).contains(q);
            if (matchName || matchPhone || matchEmail) {
                filtered.add(c);
            }
        }
        return filtered;
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
