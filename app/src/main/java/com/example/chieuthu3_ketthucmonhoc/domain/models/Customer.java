package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.Objects;

public class Customer {
    private final int id;
    private final String fullName;
    private final String email;
    private final String phoneNumber;
    private final String passwordHash;
    private final String address;
    private final long createdAt;

    public Customer(int id, String fullName, String email, String phoneNumber, String passwordHash) {
        this(id, fullName, email, phoneNumber, passwordHash, null, System.currentTimeMillis());
    }

    public Customer(int id, String fullName, String email, String phoneNumber, String passwordHash, String address, long createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.address = address;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAddress() {
        return address;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id == customer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
