package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

import java.util.List;

public interface IAuthenticationService {
    AuthResult<Customer> register(String fullName, String email, String phoneNumber, String password);
    AuthResult<Customer> loginCustomer(String phoneOrEmail, String password);
    AuthResult<AdminAccount> loginAdmin(String username, String password);
    AuthResult<Customer> updateProfile(int customerId, String fullName, String email, String address, String newPassword);
    List<Customer> getAllCustomers();
    List<Customer> searchCustomers(String query);
    void logout();
    Customer getCurrentCustomer();
    AdminAccount getCurrentAdmin();
}
