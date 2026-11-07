package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.UserRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.UserRole;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserRepositoryImplTest {

    private UserRepositoryImpl repository;

    private final Customer customer1 = new Customer(
            1,
            "Nguyễn Văn A",
            "A@Gmail.com",
            "0901234567",
            "hash-a"
    );
    private final Customer customer2 = new Customer(
            3,
            "Trần Thị B",
            "b@gmail.com",
            "0912345678",
            "hash-b"
    );
    private final AdminAccount admin = new AdminAccount(
            1,
            "QuanTri",
            "hash-admin",
            "Quản Trị Viên",
            UserRole.SUPER_ADMIN
    );

    @Before
    public void setup() {
        repository = new UserRepositoryImpl(
                Arrays.asList(customer1, customer2),
                Collections.singletonList(admin)
        );
    }

    @Test
    public void getCustomerByPhoneTrimsInputAndReturnsNullForUnknownPhone() {
        assertEquals(customer1, repository.getCustomerByPhone(" 0901234567 "));
        assertNull(repository.getCustomerByPhone("0999999999"));
    }

    @Test
    public void getCustomerByEmailIgnoresCaseAndTrimsInput() {
        assertEquals(customer1, repository.getCustomerByEmail(" a@GMAIL.COM "));
        assertNull(repository.getCustomerByEmail("unknown@gmail.com"));
    }

    @Test
    public void getAdminByUsernameIgnoresCaseAndTrimsInput() {
        assertEquals(admin, repository.getAdminByUsername(" quantri "));
        assertNull(repository.getAdminByUsername("khongtontai"));
    }

    @Test
    public void getCustomerByIdReturnsNullForUnknownId() {
        assertNull(repository.getCustomerById(99));
    }

    @Test
    public void saveCustomerWithNonPositiveIdAssignsNextIdAfterCurrentMax() {
        Customer saved = repository.saveCustomer(
                new Customer(0, "Khách Mới", "moi@gmail.com", "0933333333", "hash-c")
        );
        // max id hiện có là 3, id mới phải là 4
        assertEquals(4, saved.getId());
        assertEquals(3, repository.getAllCustomers().size());
        assertEquals(saved, repository.getCustomerById(4));
    }

    @Test
    public void saveCustomerWithExistingIdUpdatesInPlace() {
        Customer updated = new Customer(
                customer1.getId(),
                "Nguyễn Văn A (Đã cập nhật)",
                customer1.getEmail(),
                customer1.getPhoneNumber(),
                customer1.getPasswordHash()
        );
        repository.saveCustomer(updated);

        assertEquals("Không thêm phần tử mới khi cập nhật", 2, repository.getAllCustomers().size());
        assertNotNull(repository.getCustomerById(1));
        assertEquals("Nguyễn Văn A (Đã cập nhật)", repository.getCustomerById(1).getFullName());
    }

    @Test
    public void getAllAdminsReturnsTheSeededAdmins() {
        assertEquals(Collections.singletonList(admin), repository.getAllAdmins());
    }
}
