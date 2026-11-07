package com.example.chieuthu3_ketthucmonhoc.domain.services;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardMetrics {
    private final long totalRevenue;
    private final int totalOrdersCount;
    private final int completedOrdersCount;
    private final int pendingOrdersCount;
    private final int totalMenuItemsCount;
    private final int completionRatePercent;

    public DashboardMetrics(long totalRevenue, int totalOrdersCount, int completedOrdersCount,
                            int pendingOrdersCount, int totalMenuItemsCount, int completionRatePercent) {
        this.totalRevenue = totalRevenue;
        this.totalOrdersCount = totalOrdersCount;
        this.completedOrdersCount = completedOrdersCount;
        this.pendingOrdersCount = pendingOrdersCount;
        this.totalMenuItemsCount = totalMenuItemsCount;
        this.completionRatePercent = completionRatePercent;
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public int getTotalOrdersCount() {
        return totalOrdersCount;
    }

    public int getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public int getPendingOrdersCount() {
        return pendingOrdersCount;
    }

    public int getTotalMenuItemsCount() {
        return totalMenuItemsCount;
    }

    public int getCompletionRatePercent() {
        return completionRatePercent;
    }

    public String getFormattedRevenue() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(totalRevenue) + " đ";
    }
}
