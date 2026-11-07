package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

import java.util.List;

public interface IUserRepository {
    Customer getCustomerById(int id);
    Customer getCustomerByPhone(String phone);
    Customer getCustomerByEmail(String email);
    Customer saveCustomer(Customer customer);
    List<Customer> getAllCustomers();

    AdminAccount getAdminByUsername(String username);
    List<AdminAccount> getAllAdmins();
}
