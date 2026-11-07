package com.example.chieuthu3_ketthucmonhoc.data.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

@Entity(
    tableName = "customers",
    indices = {
        @Index(value = {"phone"}, unique = true),
        @Index(value = {"email"}, unique = true)
    }
)
public class CustomerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String address;
    private long createdAt;

    public CustomerEntity(int id, String fullName, String email, String phone, String passwordHash, String address, long createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.address = address;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Customer toDomain() {
        return new Customer(
                id,
                fullName,
                email != null ? email : "",
                phone,
                passwordHash,
                address,
                createdAt
        );
    }

    public static CustomerEntity fromDomain(Customer customer) {
        String safeEmail = (customer.getEmail() != null && !customer.getEmail().trim().isEmpty())
                ? customer.getEmail().trim()
                : null;
        return new CustomerEntity(
                customer.getId(),
                customer.getFullName().trim(),
                safeEmail,
                customer.getPhoneNumber().trim(),
                customer.getPasswordHash(),
                customer.getAddress(),
                customer.getCreatedAt()
        );
    }
}
