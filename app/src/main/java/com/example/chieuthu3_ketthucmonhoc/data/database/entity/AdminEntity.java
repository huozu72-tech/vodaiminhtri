package com.example.chieuthu3_ketthucmonhoc.data.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.UserRole;

@Entity(
    tableName = "admins",
    indices = {@Index(value = {"username"}, unique = true)}
)
public class AdminEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;
    private long createdAt;

    public AdminEntity(int id, String username, String passwordHash, String fullName, String role, long createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public AdminAccount toDomain() {
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role);
        } catch (Exception e) {
            userRole = UserRole.MANAGER;
        }
        return new AdminAccount(
                id,
                username,
                passwordHash,
                fullName,
                userRole,
                createdAt
        );
    }

    public static AdminEntity fromDomain(AdminAccount admin) {
        return new AdminEntity(
                admin.getId(),
                admin.getUsername().trim(),
                admin.getPasswordHash(),
                admin.getFullName(),
                admin.getRole().name(),
                admin.getCreatedAt()
        );
    }
}
