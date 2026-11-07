package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.LoyaltyProfile;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.PromoVoucher;
import com.example.chieuthu3_ketthucmonhoc.domain.models.RestaurantTable;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableServiceRequest;

import java.util.List;
import java.util.Set;

/**
 * Dịch vụ vận hành nhà hàng nâng cao:
 * - Quản lý Mã giảm giá / Voucher khuyến mãi
 * - Sơ đồ bàn trực quan & Đặt bàn trước (Table Reservation)
 * - Gọi nhân viên phục vụ tại bàn (Smart Waiter Call)
 * - Danh sách món ăn yêu thích (Wishlist)
 * - Hạng thành viên VIP & Tích điểm đổi thưởng (Loyalty Tier)
 * - Báo cáo phân tích vận hành & doanh thu chuyên sâu
 */
public interface IRestaurantOperationsService {

    // 1. Mã giảm giá / Voucher
    List<PromoVoucher> getAllVouchers();
    List<PromoVoucher> getActiveVouchers();
    PromoVoucher getVoucherByCode(String code);
    long calculateVoucherDiscount(String code, long orderSubtotal, int totalItemCount);
    boolean addOrUpdateVoucher(PromoVoucher voucher);
    boolean toggleVoucherStatus(String code);

    // 2. Sơ đồ bàn & Đặt bàn trước
    List<RestaurantTable> getAllTables();
    RestaurantTable getTableByNumber(String tableNumber);
    String getSelectedTableNumber();
    void setSelectedTableNumber(String tableNumber);
    boolean reserveTable(String tableNumber, String customerName, String phone, String time, int guests, String note);
    boolean occupyTable(String tableNumber, int guests);
    boolean markTableAvailable(String tableNumber);

    // 3. Gọi phục vụ tại bàn (Waiter Call)
    TableServiceRequest createServiceRequest(String tableNumber, String requestType, String customNote);
    List<TableServiceRequest> getPendingServiceRequests();
    List<TableServiceRequest> getAllServiceRequests();
    boolean resolveServiceRequest(int requestId);

    // 4. Món ăn yêu thích (Favorites / Wishlist)
    boolean toggleFavoriteDish(int menuItemId);
    boolean isFavoriteDish(int menuItemId);
    Set<Integer> getFavoriteDishIds();
    List<MenuItem> getFavoriteDishes();

    // 5. Hạng thành viên VIP & Tích điểm + Báo cáo phân tích
    LoyaltyProfile getCustomerLoyaltyProfile(Integer customerId);
    String generateAnalyticalReportText();
}
