package com.example.chieuthu3_ketthucmonhoc.domain.models;

public enum UserRole {
    SUPER_ADMIN("Quản trị viên cấp cao"),
    MANAGER("Quản lý nhà hàng"),
    STAFF("Nhân viên phục vụ");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
