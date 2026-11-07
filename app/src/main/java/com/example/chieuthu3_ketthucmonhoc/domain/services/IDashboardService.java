package com.example.chieuthu3_ketthucmonhoc.domain.services;

import java.util.List;

public interface IDashboardService {
    DashboardMetrics getDashboardMetrics();
    List<TopSellingItem> getTopSellingMenuItems(int limit);
    List<TopSellingItem> getTopSellingMenuItems();
}
