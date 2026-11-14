package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;

/**
 * Kết quả phiên đăng nhập dùng chung cho cả Khách hàng ({@link Customer})
 * và Quản trị viên ({@link AdminAccount}).
 */
public class UnifiedLoginResult {
    private final Customer customer;
    private final AdminAccount adminAccount;

    public UnifiedLoginResult(Customer customer) {
        this.customer = customer;
        this.adminAccount = null;
    }

    public UnifiedLoginResult(AdminAccount adminAccount) {
        this.customer = null;
        this.adminAccount = adminAccount;
    }

    public boolean isAdmin() {
        return adminAccount != null;
    }

    public Customer getCustomer() {
        return customer;
    }

    public AdminAccount getAdminAccount() {
        return adminAccount;
    }
}
