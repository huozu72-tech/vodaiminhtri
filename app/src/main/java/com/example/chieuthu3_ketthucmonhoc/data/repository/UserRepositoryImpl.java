package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserRepositoryImpl implements IUserRepository {

    private final CopyOnWriteArrayList<Customer> customers;
    private final CopyOnWriteArrayList<AdminAccount> admins;
    private int nextCustomerId;

    public UserRepositoryImpl() {
        this(InitialDataFactory.createInitialCustomers(), InitialDataFactory.createInitialAdmins());
    }

    public UserRepositoryImpl(List<Customer> initialCustomers, List<AdminAccount> initialAdmins) {
        this.customers = new CopyOnWriteArrayList<>(initialCustomers != null ? initialCustomers : new ArrayList<>());
        this.admins = new CopyOnWriteArrayList<>(initialAdmins != null ? initialAdmins : new ArrayList<>());
        int maxId = 0;
        for (Customer c : this.customers) {
            if (c.getId() > maxId) {
                maxId = c.getId();
            }
        }
        this.nextCustomerId = maxId + 1;
    }

    @Override
    public Customer getCustomerById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Customer getCustomerByPhone(String phone) {
        if (phone == null) return null;
        String query = phone.trim();
        for (Customer c : customers) {
            if (c.getPhoneNumber() != null && c.getPhoneNumber().equals(query)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        if (email == null) return null;
        String query = email.trim();
        for (Customer c : customers) {
            if (c.getEmail() != null && c.getEmail().equalsIgnoreCase(query)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public synchronized Customer saveCustomer(Customer customer) {
        Customer target;
        if (customer.getId() <= 0) {
            target = new Customer(
                    nextCustomerId++,
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getPasswordHash(),
                    customer.getAddress(),
                    customer.getCreatedAt()
            );
        } else {
            target = customer;
        }

        int existingIndex = -1;
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == target.getId()) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            customers.set(existingIndex, target);
        } else {
            customers.add(target);
        }
        return target;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    @Override
    public AdminAccount getAdminByUsername(String username) {
        if (username == null) return null;
        String query = username.trim();
        for (AdminAccount a : admins) {
            if (a.getUsername() != null && a.getUsername().equalsIgnoreCase(query)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public List<AdminAccount> getAllAdmins() {
        return new ArrayList<>(admins);
    }
}
