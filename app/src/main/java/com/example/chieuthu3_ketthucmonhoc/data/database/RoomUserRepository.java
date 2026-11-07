package com.example.chieuthu3_ketthucmonhoc.data.database;

import com.example.chieuthu3_ketthucmonhoc.data.database.dao.UserDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.AdminEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.CustomerEntity;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

import java.util.ArrayList;
import java.util.List;

public class RoomUserRepository implements IUserRepository {

    private final UserDao dao;

    public RoomUserRepository(AppDatabase database) {
        this.dao = database.userDao();
    }

    @Override
    public Customer getCustomerById(int id) {
        CustomerEntity entity = dao.getCustomerById(id);
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public Customer getCustomerByPhone(String phone) {
        if (phone == null) return null;
        CustomerEntity entity = dao.getCustomerByPhone(phone.trim());
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        if (email == null) return null;
        CustomerEntity entity = dao.getCustomerByEmail(email.trim());
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        CustomerEntity entity = CustomerEntity.fromDomain(customer);
        if (entity.getId() <= 0) {
            entity.setId(0);
            long newId = dao.insertCustomer(entity);
            return new Customer(
                    (int) newId,
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getPasswordHash(),
                    customer.getAddress(),
                    customer.getCreatedAt()
            );
        }

        CustomerEntity existing = dao.getCustomerById(entity.getId());
        if (existing == null) {
            dao.insertCustomer(entity);
        } else {
            dao.updateCustomer(entity);
        }
        return customer;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<CustomerEntity> entities = dao.getAllCustomers();
        List<Customer> result = new ArrayList<>();
        for (CustomerEntity entity : entities) {
            result.add(entity.toDomain());
        }
        return result;
    }

    @Override
    public AdminAccount getAdminByUsername(String username) {
        if (username == null) return null;
        AdminEntity entity = dao.getAdminByUsername(username.trim());
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public List<AdminAccount> getAllAdmins() {
        List<AdminEntity> entities = dao.getAllAdmins();
        List<AdminAccount> result = new ArrayList<>();
        for (AdminEntity entity : entities) {
            result.add(entity.toDomain());
        }
        return result;
    }
}
