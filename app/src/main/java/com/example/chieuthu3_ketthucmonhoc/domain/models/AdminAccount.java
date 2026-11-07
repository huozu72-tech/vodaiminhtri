package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.Objects;

public class AdminAccount {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final UserRole role;
    private final long createdAt;

    public AdminAccount(int id, String username, String passwordHash, String fullName) {
        this(id, username, passwordHash, fullName, UserRole.MANAGER, System.currentTimeMillis());
    }

    public AdminAccount(int id, String username, String passwordHash, String fullName, UserRole role) {
        this(id, username, passwordHash, fullName, role, System.currentTimeMillis());
    }

    public AdminAccount(int id, String username, String passwordHash, String fullName, UserRole role, long createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role != null ? role : UserRole.MANAGER;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminAccount that = (AdminAccount) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AdminAccount{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                '}';
    }
}
