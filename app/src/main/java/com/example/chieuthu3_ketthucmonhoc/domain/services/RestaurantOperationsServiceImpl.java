package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.LoyaltyProfile;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.PromoVoucher;
import com.example.chieuthu3_ketthucmonhoc.domain.models.RestaurantTable;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableServiceRequest;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RestaurantOperationsServiceImpl implements IRestaurantOperationsService {

    private final IMenuRepository menuRepository;
    private final IOrderRepository orderRepository;

    private final Map<String, PromoVoucher> vouchers = new LinkedHashMap<>();
    private final Map<String, RestaurantTable> tables = new LinkedHashMap<>();
    private final List<TableServiceRequest> serviceRequests = new ArrayList<>();
    private final Set<Integer> favoriteDishIds = new LinkedHashSet<>();

    private String selectedTableNumber = "12";
    private int nextRequestId = 1;

    public RestaurantOperationsServiceImpl(IMenuRepository menuRepository, IOrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        seedDefaultVouchers();
        seedDefaultTables();
        seedDefaultFavoritesAndRequests();
    }

    private void seedDefaultVouchers() {
        addVoucherInternal(new PromoVoucher(
                "CHIEUTHU3",
                "Ưu Đãi Chiều Thứ 3 (-15%)",
                "Giảm 15% tối đa 50.000đ cho mọi đơn hàng từ 100.000đ",
                15,
                50000L,
                100000L,
                1,
                true
        ));
        addVoucherInternal(new PromoVoucher(
                "TANBINH30",
                "Quà Tặng Thực Khách Mới (-30K)",
                "Giảm trực tiếp 30.000đ cho hóa đơn từ 150.000đ",
                0,
                30000L,
                150000L,
                1,
                true
        ));
        addVoucherInternal(new PromoVoucher(
                "COMBO20",
                "Đại Tiệc Gia Đình (-20%)",
                "Giảm 20% tối đa 80.000đ khi đặt từ 3 phần ăn trở lên (đơn từ 200.000đ)",
                20,
                80000L,
                200000L,
                3,
                true
        ));
        addVoucherInternal(new PromoVoucher(
                "FREEDRINK",
                "Miễn Phí Nước Giải Khát (-15K)",
                "Tặng ngay 15.000đ tiền nước uống cho đơn từ 80.000đ",
                0,
                15000L,
                80000L,
                2,
                true
        ));
    }

    private void addVoucherInternal(PromoVoucher v) {
        vouchers.put(v.getCode(), v);
    }

    private void seedDefaultTables() {
        // Khu A - Sân Vườn Thoáng Mát
        putTable(new RestaurantTable("Bàn 01", "Khu A - Sân Vườn", 2, TableStatus.AVAILABLE));
        putTable(new RestaurantTable("Bàn 02", "Khu A - Sân Vườn", 4, TableStatus.OCCUPIED, "Anh Minh", "0901234567", "12:00", 3, "Khách đang dùng bữa"));
        putTable(new RestaurantTable("Bàn 03", "Khu A - Sân Vườn", 4, TableStatus.AVAILABLE));
        putTable(new RestaurantTable("Bàn 04", "Khu A - Sân Vườn", 6, TableStatus.RESERVED, "Chị Lan", "0912345678", "18:30", 5, "Đặt trước gần hồ cá"));

        // Khu B - Phòng Máy Lạnh
        putTable(new RestaurantTable("Bàn 05", "Khu B - Máy Lạnh", 4, TableStatus.OCCUPIED, "Anh Tuấn", "0987654321", "11:45", 4, "Đang chế biến món"));
        putTable(new RestaurantTable("Bàn 06", "Khu B - Máy Lạnh", 4, TableStatus.AVAILABLE));
        putTable(new RestaurantTable("Bàn 08", "Khu B - Máy Lạnh", 6, TableStatus.AVAILABLE));
        putTable(new RestaurantTable("Bàn 12", "Khu B - Máy Lạnh", 4, TableStatus.OCCUPIED, "Khách Hàng Thân Thiết", "0901234567", "Hiện tại", 2, "Bàn đang sử dụng"));

        // Khu C - Phòng VIP Riêng Tư
        putTable(new RestaurantTable("VIP 01", "Khu C - Phòng VIP", 8, TableStatus.RESERVED, "Gia đình Chú Hùng", "0933445566", "19:00", 8, "Tiệc sinh nhật - Cần ghế trẻ em"));
        putTable(new RestaurantTable("VIP 02", "Khu C - Phòng VIP", 10, TableStatus.AVAILABLE));
    }

    private void putTable(RestaurantTable table) {
        tables.put(table.getTableNumber(), table);
    }

    private void seedDefaultFavoritesAndRequests() {
        // Mặc định đánh dấu 2 món đặc sản vào danh sách yêu thích mẫu
        favoriteDishIds.add(1); // Chân Giò Muối Chiên Giòn
        favoriteDishIds.add(2); // Lẩu Gà Lá Giang

        // Một yêu cầu gọi phục vụ mẫu tại Bàn 05 để Admin thấy ngay trên Dashboard
        serviceRequests.add(new TableServiceRequest(
                nextRequestId++,
                "Bàn 05",
                "🧊 Thêm đá / Trà đá",
                "Cho xin thêm 2 ly đá viên và khăn giấy",
                System.currentTimeMillis() - 180_000L,
                false
        ));
    }

    @Override
    public synchronized List<PromoVoucher> getAllVouchers() {
        return new ArrayList<>(vouchers.values());
    }

    @Override
    public synchronized List<PromoVoucher> getActiveVouchers() {
        List<PromoVoucher> activeList = new ArrayList<>();
        for (PromoVoucher v : vouchers.values()) {
            if (v.isActive()) {
                activeList.add(v);
            }
        }
        return activeList;
    }

    @Override
    public synchronized PromoVoucher getVoucherByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return vouchers.get(code.trim().toUpperCase());
    }

    @Override
    public synchronized long calculateVoucherDiscount(String code, long orderSubtotal, int totalItemCount) {
        PromoVoucher voucher = getVoucherByCode(code);
        if (voucher == null) {
            return 0L;
        }
        return voucher.calculateDiscount(orderSubtotal, totalItemCount);
    }

    @Override
    public synchronized boolean addOrUpdateVoucher(PromoVoucher voucher) {
        if (voucher == null || voucher.getCode().isEmpty()) {
            return false;
        }
        vouchers.put(voucher.getCode(), voucher);
        return true;
    }

    @Override
    public synchronized boolean toggleVoucherStatus(String code) {
        PromoVoucher existing = getVoucherByCode(code);
        if (existing == null) {
            return false;
        }
        vouchers.put(existing.getCode(), existing.withActive(!existing.isActive()));
        return true;
    }

    @Override
    public synchronized List<RestaurantTable> getAllTables() {
        return new ArrayList<>(tables.values());
    }

    @Override
    public synchronized RestaurantTable getTableByNumber(String tableNumber) {
        if (tableNumber == null) return null;
        String normalized = normalizeTableKey(tableNumber);
        return tables.get(normalized);
    }

    @Override
    public synchronized String getSelectedTableNumber() {
        return selectedTableNumber;
    }

    @Override
    public synchronized void setSelectedTableNumber(String tableNumber) {
        if (tableNumber != null && !tableNumber.trim().isEmpty()) {
            String raw = tableNumber.trim();
            if (raw.toLowerCase().startsWith("bàn ")) {
                this.selectedTableNumber = raw.substring(4).trim();
            } else {
                this.selectedTableNumber = raw;
            }
        }
    }

    @Override
    public synchronized boolean reserveTable(
            String tableNumber,
            String customerName,
            String phone,
            String time,
            int guests,
            String note
    ) {
        String key = normalizeTableKey(tableNumber);
        RestaurantTable existing = tables.get(key);
        if (existing == null) {
            return false;
        }
        tables.put(key, existing.withReservation(
                customerName != null ? customerName.trim() : "Khách đặt trước",
                phone != null ? phone.trim() : "",
                time != null && !time.trim().isEmpty() ? time.trim() : "19:00",
                Math.max(1, guests),
                note != null ? note.trim() : ""
        ));
        return true;
    }

    @Override
    public synchronized boolean occupyTable(String tableNumber, int guests) {
        String key = normalizeTableKey(tableNumber);
        RestaurantTable existing = tables.get(key);
        if (existing == null) {
            return false;
        }
        tables.put(key, new RestaurantTable(
                existing.getTableNumber(),
                existing.getZone(),
                existing.getCapacity(),
                TableStatus.OCCUPIED,
                existing.getReservedByName().isEmpty() ? "Khách tại bàn" : existing.getReservedByName(),
                existing.getReservedByPhone(),
                "Đang phục vụ",
                Math.max(1, guests),
                existing.getNote()
        ));
        setSelectedTableNumber(existing.getTableNumber());
        return true;
    }

    @Override
    public synchronized boolean markTableAvailable(String tableNumber) {
        String key = normalizeTableKey(tableNumber);
        RestaurantTable existing = tables.get(key);
        if (existing == null) {
            return false;
        }
        tables.put(key, existing.withStatus(TableStatus.AVAILABLE));
        return true;
    }

    private String normalizeTableKey(String input) {
        String trimmed = input.trim();
        if (tables.containsKey(trimmed)) {
            return trimmed;
        }
        if (trimmed.matches("\\d+")) {
            int num = Integer.parseInt(trimmed);
            String formatted = String.format(Locale.getDefault(), "Bàn %02d", num);
            if (tables.containsKey(formatted)) {
                return formatted;
            }
        }
        return trimmed;
    }

    @Override
    public synchronized TableServiceRequest createServiceRequest(String tableNumber, String requestType, String customNote) {
        String tbl = (tableNumber == null || tableNumber.trim().isEmpty()) ? "Bàn " + selectedTableNumber : tableNumber.trim();
        if (!tbl.toLowerCase().startsWith("bàn") && !tbl.toUpperCase().startsWith("VIP")) {
            tbl = "Bàn " + tbl;
        }
        TableServiceRequest req = new TableServiceRequest(
                nextRequestId++,
                tbl,
                requestType != null && !requestType.trim().isEmpty() ? requestType.trim() : "🛎️ Gọi nhân viên hỗ trợ",
                customNote,
                System.currentTimeMillis(),
                false
        );
        serviceRequests.add(0, req);
        return req;
    }

    @Override
    public synchronized List<TableServiceRequest> getPendingServiceRequests() {
        List<TableServiceRequest> pending = new ArrayList<>();
        for (TableServiceRequest r : serviceRequests) {
            if (!r.isResolved()) {
                pending.add(r);
            }
        }
        return pending;
    }

    @Override
    public synchronized List<TableServiceRequest> getAllServiceRequests() {
        return new ArrayList<>(serviceRequests);
    }

    @Override
    public synchronized boolean resolveServiceRequest(int requestId) {
        for (int i = 0; i < serviceRequests.size(); i++) {
            TableServiceRequest r = serviceRequests.get(i);
            if (r.getId() == requestId) {
                serviceRequests.set(i, r.withResolved(true));
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean toggleFavoriteDish(int menuItemId) {
        if (favoriteDishIds.contains(menuItemId)) {
            favoriteDishIds.remove(menuItemId);
            return false;
        } else {
            favoriteDishIds.add(menuItemId);
            return true;
        }
    }

    @Override
    public synchronized boolean isFavoriteDish(int menuItemId) {
        return favoriteDishIds.contains(menuItemId);
    }

    @Override
    public synchronized Set<Integer> getFavoriteDishIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(favoriteDishIds));
    }

    @Override
    public synchronized List<MenuItem> getFavoriteDishes() {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuRepository.getAllMenuItems()) {
            if (favoriteDishIds.contains(item.getId())) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public LoyaltyProfile getCustomerLoyaltyProfile(Integer customerId) {
        long totalSpent = 0L;
        int completedCount = 0;

        if (customerId != null) {
            List<Order> orders = orderRepository.getOrdersByCustomer(customerId);
            if (orders != null) {
                for (Order o : orders) {
                    if (o.getStatus() != OrderStatus.CANCELLED) {
                        totalSpent += o.getTotalAmount();
                        completedCount++;
                    }
                }
            }
        }

        int points = (int) (totalSpent / 10000L);
        String tierName;
        int bonusDiscount;
        String nextTier;
        long amountToNext;
        int progress;

        if (totalSpent >= 1_500_000L) {
            tierName = "💎 VIP Kim Cương (Diamond)";
            bonusDiscount = 8;
            nextTier = "Đã đạt hạng cao nhất";
            amountToNext = 0L;
            progress = 100;
        } else if (totalSpent >= 700_000L) {
            tierName = "🥇 VIP Vàng (Gold)";
            bonusDiscount = 5;
            nextTier = "💎 VIP Kim Cương";
            amountToNext = 1_500_000L - totalSpent;
            progress = (int) (((totalSpent - 700_000L) * 100L) / 800_000L);
        } else if (totalSpent >= 300_000L) {
            tierName = "🥈 VIP Bạc (Silver)";
            bonusDiscount = 3;
            nextTier = "🥇 VIP Vàng";
            amountToNext = 700_000L - totalSpent;
            progress = (int) (((totalSpent - 300_000L) * 100L) / 400_000L);
        } else {
            tierName = "🥉 Thành Viên Đồng (Member)";
            bonusDiscount = 0;
            nextTier = "🥈 VIP Bạc";
            amountToNext = 300_000L - totalSpent;
            progress = (int) ((totalSpent * 100L) / 300_000L);
        }

        return new LoyaltyProfile(
                tierName,
                points,
                totalSpent,
                completedCount,
                bonusDiscount,
                nextTier,
                amountToNext,
                progress
        );
    }

    @Override
    public String generateAnalyticalReportText() {
        List<Order> allOrders = orderRepository.getAllOrders();
        List<MenuItem> allMenu = menuRepository.getAllMenuItems();

        long completedRevenue = 0L;
        long pendingRevenue = 0L;
        int completedCount = 0;
        int pendingCount = 0;
        int cancelledCount = 0;

        long mainCategoryRev = 0L;
        long sideCategoryRev = 0L;
        long drinkCategoryRev = 0L;

        for (Order o : allOrders) {
            if (o.getStatus() == OrderStatus.COMPLETED) {
                completedRevenue += o.getTotalAmount();
                completedCount++;
            } else if (o.getStatus() == OrderStatus.CANCELLED) {
                cancelledCount++;
            } else {
                pendingRevenue += o.getTotalAmount();
                pendingCount++;
            }

            if (o.getStatus() != OrderStatus.CANCELLED) {
                for (OrderItem oi : o.getItems()) {
                    MenuItem mi = oi.getMenuItem();
                    if (mi == null) continue;
                    long sub = oi.getSubtotal();
                    if (mi.getCategory() == FoodCategory.MAIN_DISH) mainCategoryRev += sub;
                    else if (mi.getCategory() == FoodCategory.SIDE_DISH) sideCategoryRev += sub;
                    else if (mi.getCategory() == FoodCategory.BEVERAGE) drinkCategoryRev += sub;
                }
            }
        }

        int occupiedTables = 0;
        int reservedTables = 0;
        int availableTables = 0;
        for (RestaurantTable t : getAllTables()) {
            if (t.getStatus() == TableStatus.OCCUPIED) occupiedTables++;
            else if (t.getStatus() == TableStatus.RESERVED) reservedTables++;
            else availableTables++;
        }

        String timestamp = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📊 BÁO CÁO PHÂN TÍCH VẬN HÀNH & DOANH THU\n");
        sb.append("Nhà hàng: Ẩm Thực Việt Chiều Thứ 3\n");
        sb.append("Thời điểm kết xuất: ").append(timestamp).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("1. TỔNG QUAN DOANH THU & ĐƠN HÀNG:\n");
        sb.append(" • Doanh thu đã quyết toán: ").append(String.format(Locale.getDefault(), "%,d đ", completedRevenue)).append("\n");
        sb.append(" • Doanh thu đơn đang phục vụ: ").append(String.format(Locale.getDefault(), "%,d đ", pendingRevenue)).append("\n");
        sb.append(" • Tổng đơn hàng: ").append(allOrders.size())
                .append(" (Hoàn thành: ").append(completedCount)
                .append(" | Đang xử lý: ").append(pendingCount)
                .append(" | Đã hủy: ").append(cancelledCount).append(")\n\n");

        sb.append("2. CƠ CẤU DOANH THU THEO NHÓM THỰC ĐƠN:\n");
        sb.append(" • 🍲 Món chính: ").append(String.format(Locale.getDefault(), "%,d đ", mainCategoryRev)).append("\n");
        sb.append(" • 🥗 Món phụ / Khai vị: ").append(String.format(Locale.getDefault(), "%,d đ", sideCategoryRev)).append("\n");
        sb.append(" • 🥤 Nước uống giải khát: ").append(String.format(Locale.getDefault(), "%,d đ", drinkCategoryRev)).append("\n");
        sb.append(" • Tổng số món trên thực đơn: ").append(allMenu.size()).append(" món\n\n");

        sb.append("3. CÔNG SUẤT BÀN & PHỤC VỤ TẠI CHỖ:\n");
        sb.append(" • Tổng số bàn: ").append(tables.size()).append(" bàn\n");
        sb.append(" • Đang có khách: ").append(occupiedTables)
                .append(" | Đã đặt trước: ").append(reservedTables)
                .append(" | Bàn trống: ").append(availableTables).append("\n");
        sb.append(" • Yêu cầu gọi phục vụ chờ xử lý: ").append(getPendingServiceRequests().size()).append("\n\n");

        sb.append("4. CHƯƠNG TRÌNH KHUYẾN MÃI (VOUCHER):\n");
        for (PromoVoucher v : getAllVouchers()) {
            sb.append(" • [").append(v.getCode()).append("] ").append(v.getTitle())
                    .append(" — ").append(v.isActive() ? "Đang bật" : "Tạm ngưng").append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return sb.toString();
    }
}
