package com.example.chieuthu3_ketthucmonhoc.domain.models

enum class UserRole(val displayName: String) {
    SUPER_ADMIN("Quản trị viên cấp cao"),
    MANAGER("Quản lý nhà hàng"),
    STAFF("Nhân viên phục vụ")
}
