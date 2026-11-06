package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.UserRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer
import com.example.chieuthu3_ketthucmonhoc.domain.models.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Khóa hành vi của UserRepositoryImpl in-memory (trước đây chưa có test):
 * tra cứu email/username không phân biệt hoa thường và tự trim input,
 * tra cứu SĐT có trim, cấp id khách tự tăng theo max+1,
 * và save với id đã có thì cập nhật tại chỗ —
 * đúng hợp đồng mà AuthenticationServiceImpl đang dựa vào.
 */
class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl

    private val customer1 = Customer(
        id = 1,
        fullName = "Nguyễn Văn A",
        email = "A@Gmail.com",
        phoneNumber = "0901234567",
        passwordHash = "hash-a"
    )
    private val customer2 = Customer(
        id = 3,
        fullName = "Trần Thị B",
        email = "b@gmail.com",
        phoneNumber = "0912345678",
        passwordHash = "hash-b"
    )
    private val admin = AdminAccount(
        id = 1,
        username = "QuanTri",
        passwordHash = "hash-admin",
        fullName = "Quản Trị Viên",
        role = UserRole.SUPER_ADMIN
    )

    @Before
    fun setup() {
        repository = UserRepositoryImpl(
            initialCustomers = listOf(customer1, customer2),
            initialAdmins = listOf(admin)
        )
    }

    @Test
    fun `getCustomerByPhone trims input and returns null for unknown phone`() {
        assertEquals(customer1, repository.getCustomerByPhone(" 0901234567 "))
        assertNull(repository.getCustomerByPhone("0999999999"))
    }

    @Test
    fun `getCustomerByEmail ignores case and trims input`() {
        assertEquals(customer1, repository.getCustomerByEmail(" a@GMAIL.COM "))
        assertNull(repository.getCustomerByEmail("unknown@gmail.com"))
    }

    @Test
    fun `getAdminByUsername ignores case and trims input`() {
        assertEquals(admin, repository.getAdminByUsername(" quantri "))
        assertNull(repository.getAdminByUsername("khongtontai"))
    }

    @Test
    fun `getCustomerById returns null for unknown id`() {
        assertNull(repository.getCustomerById(99))
    }

    @Test
    fun `saveCustomer with non-positive id assigns next id after current max`() {
        val saved = repository.saveCustomer(
            Customer(0, "Khách Mới", "moi@gmail.com", "0933333333", "hash-c")
        )
        // max id hiện có là 3 (không phải số phần tử), id mới phải là 4
        assertEquals(4, saved.id)
        assertEquals(3, repository.getAllCustomers().size)
        assertEquals(saved, repository.getCustomerById(4))
    }

    @Test
    fun `saveCustomer with existing id updates in place`() {
        repository.saveCustomer(customer1.copy(fullName = "Nguyễn Văn A (Đã cập nhật)"))
        assertEquals("Không thêm phần tử mới khi cập nhật", 2, repository.getAllCustomers().size)
        assertEquals("Nguyễn Văn A (Đã cập nhật)", repository.getCustomerById(1)!!.fullName)
    }

    @Test
    fun `getAllAdmins returns the seeded admins`() {
        assertEquals(listOf(admin), repository.getAllAdmins())
    }
}
