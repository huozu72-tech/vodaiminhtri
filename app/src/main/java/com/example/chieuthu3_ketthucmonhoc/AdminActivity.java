package com.example.chieuthu3_ketthucmonhoc;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chieuthu3_ketthucmonhoc.adapters.AdminCustomerAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.AdminFoodAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.AdminOrderAdapter;
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityAdminBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAddEditFoodBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAdminOrderDetailBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.LoyaltyProfile;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.PromoVoucher;
import com.example.chieuthu3_ketthucmonhoc.domain.models.RestaurantTable;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableServiceRequest;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardMetrics;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IDashboardService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IRestaurantOperationsService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.TopSellingItem;
import com.example.chieuthu3_ketthucmonhoc.utils.FoodImageLoader;
import com.example.chieuthu3_ketthucmonhoc.utils.VietnameseTelexHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private AdminFoodAdapter adminFoodAdapter;
    private AdminOrderAdapter adminOrderAdapter;
    private AdminCustomerAdapter adminCustomerAdapter;

    // Services via AppServiceLocator (Clean OOP Architecture)
    private IMenuService menuService;
    private IOrderService orderService;
    private IDashboardService dashboardService;
    private IAuthenticationService authService;
    private IRestaurantOperationsService opsService;

    private interface OnFoodImagePickedCallback {
        void onImagePicked(String savedPath);
    }

    private OnFoodImagePickedCallback activeImagePickedCallback;

    private final ActivityResultLauncher<String> pickFoodImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                String savedPath = FoodImageLoader.savePickedImageToInternalStorage(this, uri);
                if (savedPath == null || savedPath.isEmpty()) {
                    Toast.makeText(this, "Không thể đọc tệp ảnh đã chọn. Vui lòng thử ảnh khác!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (activeImagePickedCallback != null) {
                    activeImagePickedCallback.onImagePicked(savedPath);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize services
        authService = AppServiceLocator.getAuthService();
        menuService = AppServiceLocator.getMenuService();
        orderService = AppServiceLocator.getOrderService();
        dashboardService = AppServiceLocator.getDashboardService();
        opsService = AppServiceLocator.getOperationsService();

        // Cổng kiểm tra phiên quản trị: chỉ cho phép vào khi đã đăng nhập qua authService.loginAdmin()
        if (authService.getCurrentAdmin() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập quản trị để truy cập màn hình này!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupTabs();
        setupFoodRecyclerView();
        setupOrderRecyclerView();
        setupCustomerRecyclerView();
        setupSearchAndFilters();
        setupListeners();
        refreshDashboardMetrics();
        refreshAdminTablesView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardMetrics();
        refreshAdminProductsList();
        refreshAdminOrdersList();
        refreshAdminCustomersList();
        refreshAdminTablesView();
    }

    private void setupTabs() {
        binding.chipGroupAdminTabs.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            binding.layoutTabDashboard.setVisibility(View.GONE);
            binding.layoutTabProducts.setVisibility(View.GONE);
            binding.layoutTabOrders.setVisibility(View.GONE);
            binding.layoutTabTables.setVisibility(View.GONE);
            binding.layoutTabCustomers.setVisibility(View.GONE);

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipTabProducts) {
                binding.layoutTabProducts.setVisibility(View.VISIBLE);
                refreshAdminProductsList();
            } else if (checkedId == R.id.chipTabOrders) {
                binding.layoutTabOrders.setVisibility(View.VISIBLE);
                refreshAdminOrdersList();
            } else if (checkedId == R.id.chipTabTables) {
                binding.layoutTabTables.setVisibility(View.VISIBLE);
                refreshAdminTablesView();
            } else if (checkedId == R.id.chipTabCustomers) {
                binding.layoutTabCustomers.setVisibility(View.VISIBLE);
                refreshAdminCustomersList();
            } else {
                binding.layoutTabDashboard.setVisibility(View.VISIBLE);
                refreshDashboardMetrics();
            }
        });
    }

    private void refreshDashboardMetrics() {
        DashboardMetrics metrics = dashboardService.getDashboardMetrics();
        binding.tvStatTotalOrders.setText(String.valueOf(metrics.getTotalOrdersCount()));
        binding.tvStatPendingOrders.setText(String.valueOf(metrics.getPendingOrdersCount()));
        binding.tvStatTotalProducts.setText(String.valueOf(metrics.getTotalMenuItemsCount()));
        binding.tvStatTotalRevenue.setText(metrics.getFormattedRevenue());
        binding.tvStatCompletionRate.setText(
                String.format(
                        Locale.getDefault(),
                        "Tỷ Lệ Hoàn Thành Đơn: %d%% 🎯 (%d/%d đơn hoàn tất)",
                        metrics.getCompletionRatePercent(),
                        metrics.getCompletedOrdersCount(),
                        metrics.getTotalOrdersCount()
                )
        );

        AdminAccount currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin != null) {
            binding.tvAdminGreeting.setText(currentAdmin.getFullName() + " (" + currentAdmin.getRole().getDisplayName() + ")");
        } else {
            binding.tvAdminGreeting.setText("Quản Trị Viên");
        }

        // Chuông gọi phục vụ tại bàn (Table Service Requests)
        binding.layoutServiceRequestsList.removeAllViews();
        List<TableServiceRequest> pendingRequests = opsService.getPendingServiceRequests();
        binding.tvPendingRequestsBadge.setText(pendingRequests.size() + " yêu cầu");
        if (pendingRequests.isEmpty()) {
            TextView tvNoReq = new TextView(this);
            tvNoReq.setText("✅ Tất cả yêu cầu hỗ trợ tại bàn đã được phục vụ hoàn tất.");
            tvNoReq.setTextSize(12f);
            tvNoReq.setTextColor(Color.parseColor("#2E7D32"));
            binding.layoutServiceRequestsList.addView(tvNoReq);
        } else {
            for (TableServiceRequest req : pendingRequests) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(16, 12, 16, 12);
                row.setBackgroundResource(R.drawable.bg_card_food);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(0, 6, 0, 6);
                row.setLayoutParams(lp);

                TextView tvReqInfo = new TextView(this);
                tvReqInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                String noteSuffix = (req.getCustomNote() != null && !req.getCustomNote().isEmpty())
                        ? "\n   💬 Ghi chú: " + req.getCustomNote()
                        : "";
                tvReqInfo.setText(
                        "🔔 " + req.getTableNumber() + " (" + req.getFormattedTime() + ")\n"
                                + "   Yêu cầu: " + req.getRequestType() + noteSuffix
                );
                tvReqInfo.setTextSize(12f);
                tvReqInfo.setTextColor(Color.parseColor("#BF360C"));
                tvReqInfo.setTypeface(null, Typeface.BOLD);

                MaterialButton btnResolve = new MaterialButton(this);
                btnResolve.setText("✔ Đã Phục Vụ");
                btnResolve.setTextSize(11f);
                btnResolve.setAllCaps(false);
                btnResolve.setOnClickListener(v -> {
                    opsService.resolveServiceRequest(req.getId());
                    Toast.makeText(this, "Đã xác nhận phục vụ " + req.getTableNumber() + "!", Toast.LENGTH_SHORT).show();
                    refreshDashboardMetrics();
                });

                row.addView(tvReqInfo);
                row.addView(btnResolve);
                binding.layoutServiceRequestsList.addView(row);
            }
        }

        // Top 5 Best Selling Dishes (Hình 26 & UC11 Thống kê doanh thu)
        binding.layoutTopSellingDishes.removeAllViews();
        List<TopSellingItem> topItems = dashboardService.getTopSellingMenuItems(5);
        if (topItems.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có dữ liệu món bán chạy.");
            tvEmpty.setTextSize(12f);
            binding.layoutTopSellingDishes.addView(tvEmpty);
        } else {
            for (int i = 0; i < topItems.size(); i++) {
                TopSellingItem top = topItems.get(i);
                MenuItem dish = top.getMenuItem();
                long dishRevenue = dish.getPrice() * top.getQuantitySold();

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(16, 12, 16, 12);
                row.setBackgroundResource(R.drawable.bg_card_food);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 6, 0, 6);
                row.setLayoutParams(params);

                TextView tvRank = new TextView(this);
                tvRank.setText("#" + (i + 1));
                tvRank.setTextSize(14f);
                tvRank.setTypeface(null, Typeface.BOLD);
                tvRank.setTextColor(ContextCompat.getColor(this, R.color.admin_blue_accent));
                tvRank.setPadding(0, 0, 20, 0);

                TextView tvDishInfo = new TextView(this);
                tvDishInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvDishInfo.setText(
                        dish.getName() + " (" + dish.getCategory().getDisplayName() + ")\n"
                                + String.format(Locale.getDefault(), "Đã bán: %d phần • Doanh thu: %,d đ", top.getQuantitySold(), dishRevenue)
                );
                tvDishInfo.setTextSize(12f);
                tvDishInfo.setTextColor(ContextCompat.getColor(this, R.color.black));

                row.addView(tvRank);
                row.addView(tvDishInfo);
                binding.layoutTopSellingDishes.addView(row);
            }
        }
    }

    /**
     * Tab 4: Quản lý sơ đồ bàn & Đặt chỗ trực quan
     */
    private void refreshAdminTablesView() {
        List<RestaurantTable> tables = opsService.getAllTables();
        int available = 0;
        int occupied = 0;
        int reserved = 0;
        for (RestaurantTable t : tables) {
            if (t.getStatus() == TableStatus.AVAILABLE) available++;
            else if (t.getStatus() == TableStatus.OCCUPIED) occupied++;
            else if (t.getStatus() == TableStatus.RESERVED) reserved++;
        }

        binding.tvTableOccupancySummary.setText(
                String.format(
                        Locale.getDefault(),
                        "Trống: %d 🟢 • Có khách: %d 🔴 • Đặt trước: %d 🟠",
                        available, occupied, reserved
                )
        );

        binding.layoutAdminTablesContainer.removeAllViews();
        for (RestaurantTable table : tables) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 20);
            card.setLayoutParams(cardParams);
            card.setRadius(24f);
            card.setCardElevation(3f);

            int bgColor;
            int strokeColor;
            if (table.getStatus() == TableStatus.AVAILABLE) {
                bgColor = Color.parseColor("#F1F8E9");
                strokeColor = Color.parseColor("#81C784");
            } else if (table.getStatus() == TableStatus.OCCUPIED) {
                bgColor = Color.parseColor("#FFEBEE");
                strokeColor = Color.parseColor("#E57373");
            } else {
                bgColor = Color.parseColor("#FFF3E0");
                strokeColor = Color.parseColor("#FFB74D");
            }
            card.setCardBackgroundColor(bgColor);
            card.setStrokeColor(strokeColor);
            card.setStrokeWidth(2);

            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(28, 22, 28, 22);

            TextView tvTitle = new TextView(this);
            tvTitle.setText("🪑 " + table.getTableNumber() + " — " + table.getZone() + " (" + table.getCapacity() + " ghế)");
            tvTitle.setTextSize(15f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setTextColor(Color.parseColor("#212121"));

            TextView tvStatus = new TextView(this);
            tvStatus.setText("Trạng thái: " + table.getStatus().getDisplayName());
            tvStatus.setTextSize(13f);
            tvStatus.setTypeface(null, Typeface.BOLD);
            try {
                tvStatus.setTextColor(Color.parseColor(table.getStatus().getColorHex()));
            } catch (Exception ignored) {}

            box.addView(tvTitle);
            box.addView(tvStatus);

            if (table.getStatus() == TableStatus.RESERVED) {
                TextView tvResDetail = new TextView(this);
                tvResDetail.setText(
                        "📌 Khách đặt: " + table.getReservedByName() + " (" + table.getReservedByPhone() + ")\n"
                                + "⏰ Khung giờ: " + table.getReservationTime() + " • Số khách: " + table.getGuestCount() + " người"
                                + (!table.getNote().isEmpty() ? "\n📝 Ghi chú: " + table.getNote() : "")
                );
                tvResDetail.setTextSize(12f);
                tvResDetail.setTextColor(Color.parseColor("#5D4037"));
                tvResDetail.setPadding(0, 6, 0, 6);
                box.addView(tvResDetail);
            }

            LinearLayout actionsRow = new LinearLayout(this);
            actionsRow.setOrientation(LinearLayout.HORIZONTAL);
            actionsRow.setPadding(0, 12, 0, 0);

            if (table.getStatus() != TableStatus.AVAILABLE) {
                MaterialButton btnFree = new MaterialButton(this);
                btnFree.setText("🟢 Dọn Trống Bàn");
                btnFree.setTextSize(11f);
                btnFree.setAllCaps(false);
                LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                p1.setMargins(0, 0, 8, 0);
                btnFree.setLayoutParams(p1);
                btnFree.setOnClickListener(v -> {
                    opsService.markTableAvailable(table.getTableNumber());
                    Toast.makeText(this, "Đã dọn trống " + table.getTableNumber() + "!", Toast.LENGTH_SHORT).show();
                    refreshAdminTablesView();
                });
                actionsRow.addView(btnFree);
            }

            if (table.getStatus() != TableStatus.OCCUPIED) {
                MaterialButton btnOccupy = new MaterialButton(this);
                btnOccupy.setText("🔴 Nhận Khách Vào Bàn");
                btnOccupy.setTextSize(11f);
                btnOccupy.setAllCaps(false);
                LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                p2.setMargins(8, 0, 0, 0);
                btnOccupy.setLayoutParams(p2);
                btnOccupy.setOnClickListener(v -> {
                    opsService.occupyTable(table.getTableNumber(), table.getCapacity());
                    Toast.makeText(this, "Đã chuyển " + table.getTableNumber() + " sang Đang Phục Vụ!", Toast.LENGTH_SHORT).show();
                    refreshAdminTablesView();
                });
                actionsRow.addView(btnOccupy);
            }

            box.addView(actionsRow);
            card.addView(box);
            binding.layoutAdminTablesContainer.addView(card);
        }
    }

    private void setupFoodRecyclerView() {
        adminFoodAdapter = new AdminFoodAdapter(
                menuService.getAllMenuItems(),
                new AdminFoodAdapter.OnFoodActionListener() {
                    @Override
                    public void onEdit(MenuItem item) {
                        showAddEditFoodDialog(item);
                    }

                    @Override
                    public void onDelete(MenuItem item) {
                        confirmDeleteFood(item);
                    }

                    @Override
                    public void onToggleAvailability(MenuItem item) {
                        MenuItem updated = menuService.toggleAvailability(item.getId());
                        refreshAdminProductsList();
                        if (updated != null) {
                            String statusLabel = updated.isAvailable() ? "Còn phục vụ 🟢" : "Tạm hết hàng 🔴";
                            Toast.makeText(
                                    AdminActivity.this,
                                    "Đã chuyển '" + updated.getName() + "' sang trạng thái: " + statusLabel,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
        binding.rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminProducts.setAdapter(adminFoodAdapter);
    }

    private void setupOrderRecyclerView() {
        adminOrderAdapter = new AdminOrderAdapter(
                orderService.getAllOrders(),
                this::showChangeOrderStatusDialog,
                this::showOrderDetailPopup
        );
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminOrders.setAdapter(adminOrderAdapter);
    }

    private void setupCustomerRecyclerView() {
        adminCustomerAdapter = new AdminCustomerAdapter(
                authService.getAllCustomers(),
                customerId -> orderService.getCustomerOrders(customerId),
                this::showCustomerOrderHistoryPopup
        );
        binding.rvAdminCustomers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminCustomers.setAdapter(adminCustomerAdapter);
    }

    private void setupSearchAndFilters() {
        VietnameseTelexHelper.attachToEditText(binding.etSearchAdminFood, null);
        VietnameseTelexHelper.attachToEditText(binding.etSearchAdminCustomer, null);
        binding.etSearchAdminFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshAdminProductsList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupOrderStatusFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            refreshAdminOrdersList();
        });

        binding.etSearchAdminCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshAdminCustomersList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshAdminProductsList() {
        String query = binding.etSearchAdminFood.getText() != null
                ? binding.etSearchAdminFood.getText().toString().trim()
                : "";
        if (query.isEmpty()) {
            adminFoodAdapter.updateList(menuService.getAllMenuItems());
        } else {
            adminFoodAdapter.updateList(menuService.searchMenuItems(query));
        }
    }

    private void refreshAdminOrdersList() {
        List<Order> all = orderService.getAllOrders();
        int checkedId = binding.chipGroupOrderStatusFilter.getCheckedChipId();
        OrderStatus targetStatus = null;
        if (checkedId == R.id.chipOrderFilterPending) {
            targetStatus = OrderStatus.PENDING;
        } else if (checkedId == R.id.chipOrderFilterPreparing) {
            targetStatus = OrderStatus.PREPARING;
        } else if (checkedId == R.id.chipOrderFilterCompleted) {
            targetStatus = OrderStatus.COMPLETED;
        } else if (checkedId == R.id.chipOrderFilterCancelled) {
            targetStatus = OrderStatus.CANCELLED;
        }

        if (targetStatus == null) {
            adminOrderAdapter.updateList(all);
        } else {
            List<Order> filtered = new ArrayList<>();
            for (Order o : all) {
                if (o.getStatus() == targetStatus) {
                    filtered.add(o);
                }
            }
            adminOrderAdapter.updateList(filtered);
        }
    }

    private void refreshAdminCustomersList() {
        String query = binding.etSearchAdminCustomer.getText() != null
                ? binding.etSearchAdminCustomer.getText().toString().trim()
                : "";
        List<Customer> customers = authService.searchCustomers(query);
        adminCustomerAdapter.updateList(customers);
        binding.tvAdminCustomerCountHeader.setText(
                String.format(Locale.getDefault(), "Danh Sách Khách Hàng Thành Viên (%d)", customers.size())
        );
    }

    private void setupListeners() {
        binding.btnSwitchToUserApp.setOnClickListener(v -> finish());

        binding.btnSwitchToUserApp.setOnLongClickListener(v -> {
            new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Đăng Xuất Quản Trị")
                    .setMessage("Bạn có muốn đăng xuất tài khoản quản trị không?")
                    .setPositiveButton("Đăng Xuất", (dialog, which) -> {
                        authService.logout();
                        Toast.makeText(AdminActivity.this, "Đã đăng xuất quản trị!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });

        binding.tvAdminGreeting.setOnClickListener(v -> {
            AdminAccount admin = authService.getCurrentAdmin();
            String name = admin != null ? admin.getFullName() : "Admin";
            String roleName = (admin != null && admin.getRole() != null) ? admin.getRole().getDisplayName() : "Quản trị viên";

            new AlertDialog.Builder(AdminActivity.this)
                    .setTitle("Tài Khoản Quản Trị")
                    .setMessage("Đang đăng nhập: " + name + "\nVai trò: " + roleName)
                    .setPositiveButton("Đăng Xuất", (dialog, which) -> {
                        authService.logout();
                        Toast.makeText(AdminActivity.this, "Đã đăng xuất quản trị!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });

        binding.btnAddProduct.setOnClickListener(v -> showAddEditFoodDialog(null));

        binding.btnAdminManageVouchers.setOnClickListener(v -> showAdminManageVouchersDialog());

        binding.btnAdminExportReport.setOnClickListener(v -> showAdminAnalyticalReportDialog());
    }

    /**
     * Quản lý Kho Voucher Khuyến Mãi (Bật/Tắt & Tạo mã giảm giá mới)
     */
    private void showAdminManageVouchersDialog() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 28, 36, 20);
        scrollView.addView(root);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🎟️ Quản Lý Voucher Khuyến Mãi")
                .setView(scrollView)
                .setPositiveButton("➕ Tạo Mã Mới", null)
                .setNegativeButton("Đóng", null)
                .create();

        Runnable populateVouchers = () -> {
            root.removeAllViews();
            List<PromoVoucher> vouchers = opsService.getAllVouchers();
            for (PromoVoucher v : vouchers) {
                MaterialCardView card = new MaterialCardView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(0, 0, 0, 18);
                card.setLayoutParams(lp);
                card.setRadius(20f);
                card.setCardBackgroundColor(v.isActive() ? Color.parseColor("#FFF8E1") : Color.parseColor("#F5F5F5"));

                LinearLayout box = new LinearLayout(this);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setPadding(24, 18, 24, 18);

                TextView tvCode = new TextView(this);
                tvCode.setText("🏷️ " + v.getCode() + " — " + v.getTitle());
                tvCode.setTextSize(14f);
                tvCode.setTypeface(null, Typeface.BOLD);
                tvCode.setTextColor(v.isActive() ? Color.parseColor("#E65100") : Color.parseColor("#757575"));

                TextView tvDesc = new TextView(this);
                tvDesc.setText(
                        v.getDescription() + "\n"
                                + String.format(Locale.getDefault(), "Giảm %d%% (tối đa %,dđ) • Đơn tối thiểu %,dđ",
                                v.getDiscountPercent(), v.getMaxDiscountAmount(), v.getMinOrderAmount())
                );
                tvDesc.setTextSize(12f);
                tvDesc.setTextColor(Color.parseColor("#424242"));
                tvDesc.setPadding(0, 4, 0, 8);

                MaterialButton btnToggle = new MaterialButton(this);
                btnToggle.setText(v.isActive() ? "🟢 Đang kích hoạt (Nhấn để Tạm dừng)" : "🔴 Đang tạm dừng (Nhấn để Bật lại)");
                btnToggle.setTextSize(11f);
                btnToggle.setAllCaps(false);
                btnToggle.setOnClickListener(btn -> {
                    opsService.toggleVoucherStatus(v.getCode());
                    dialog.dismiss();
                    showAdminManageVouchersDialog();
                });

                box.addView(tvCode);
                box.addView(tvDesc);
                box.addView(btnToggle);
                card.addView(box);
                root.addView(card);
            }
        };

        populateVouchers.run();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            dialog.dismiss();
            showCreateNewVoucherDialog();
        });
    }

    private void showCreateNewVoucherDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(48, 24, 48, 12);

        EditText etCode = new EditText(this);
        etCode.setHint("Mã Voucher (VD: SUMMER25)");

        EditText etTitle = new EditText(this);
        etTitle.setHint("Tiêu đề ưu đãi (VD: Giảm 25% Mùa Hè)");

        EditText etPercent = new EditText(this);
        etPercent.setHint("Phần trăm giảm (1 - 50)");
        etPercent.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etMaxDiscount = new EditText(this);
        etMaxDiscount.setHint("Giảm tối đa VNĐ (VD: 40000)");
        etMaxDiscount.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etMinOrder = new EditText(this);
        etMinOrder.setHint("Đơn tối thiểu VNĐ (VD: 100000)");
        etMinOrder.setInputType(InputType.TYPE_CLASS_NUMBER);

        form.addView(etCode);
        form.addView(etTitle);
        form.addView(etPercent);
        form.addView(etMaxDiscount);
        form.addView(etMinOrder);

        new AlertDialog.Builder(this)
                .setTitle("➕ Tạo Mã Khuyến Mãi Mới")
                .setView(form)
                .setPositiveButton("Lưu Voucher", (d, w) -> {
                    String code = etCode.getText().toString().trim().toUpperCase(Locale.ROOT);
                    String title = etTitle.getText().toString().trim();
                    int percent = 10;
                    long maxDisc = 30000L;
                    long minOrd = 50000L;
                    try {
                        percent = Integer.parseInt(etPercent.getText().toString().trim());
                        maxDisc = Long.parseLong(etMaxDiscount.getText().toString().trim());
                        minOrd = Long.parseLong(etMinOrder.getText().toString().trim());
                    } catch (Exception ignored) {}

                    if (code.isEmpty() || title.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập mã và tiêu đề voucher!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PromoVoucher newVoucher = new PromoVoucher(
                            code,
                            title,
                            String.format(Locale.getDefault(), "Giảm %d%% tối đa %,dđ cho đơn từ %,dđ", percent, maxDisc, minOrd),
                            Math.max(1, Math.min(percent, 80)),
                            maxDisc,
                            minOrd,
                            1,
                            true
                    );
                    opsService.addOrUpdateVoucher(newVoucher);
                    Toast.makeText(this, "Đã phát hành mã ưu đãi " + code + "!", Toast.LENGTH_SHORT).show();
                    showAdminManageVouchersDialog();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xuất báo cáo phân tích vận hành nhà hàng toàn diện & Sao chép vào Clipboard
     */
    private void showAdminAnalyticalReportDialog() {
        String reportText = opsService.generateAnalyticalReportText();
        new AlertDialog.Builder(this)
                .setTitle("📄 Báo Cáo Tổng Kết & Phân Tích Nhà Hàng")
                .setMessage(reportText)
                .setPositiveButton("📋 Sao Chép Báo Cáo", (d, w) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("BaoCaoNhaHang", reportText));
                        Toast.makeText(this, "✅ Đã sao chép toàn bộ báo cáo vào bộ nhớ tạm!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * Hình 29: Giao diện popup xem chi tiết đơn hàng của khách hàng & In hóa đơn
     */
    private void showOrderDetailPopup(Order order) {
        DialogAdminOrderDetailBinding dialogBinding = DialogAdminOrderDetailBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.tvDetailOrderStatus.setText(order.getStatus().getDisplayName());
        try {
            dialogBinding.tvDetailOrderStatus.setTextColor(Color.parseColor(order.getStatus().getColorHex()));
        } catch (Exception ignored) {}

        dialogBinding.tvDetailOrderCodeAndTable.setText(
                "Mã đơn: " + order.getOrderId() + " • Phục vụ tại: " + order.getTableNumber()
        );

        String customerLabel = "Khách vãng lai tại bàn";
        if (order.getCustomerId() != null) {
            for (Customer c : authService.getAllCustomers()) {
                if (c.getId() == order.getCustomerId()) {
                    customerLabel = c.getFullName() + " (" + c.getPhoneNumber() + ")";
                    break;
                }
            }
        }
        dialogBinding.tvDetailCustomerInfo.setText("Khách hàng: " + customerLabel);
        dialogBinding.tvDetailTimeAndPayment.setText(
                "Thời gian: " + order.getFormattedTime() + " • Thanh toán: " + order.getPaymentMethod()
        );

        dialogBinding.layoutOrderItemsList.removeAllViews();
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(12, 10, 12, 10);

            TextView tvItemName = new TextView(this);
            tvItemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvItemName.setText((i + 1) + ". " + item.getMenuItem().getName() + "  (x" + item.getQuantity() + ")");
            tvItemName.setTextSize(13f);
            tvItemName.setTextColor(ContextCompat.getColor(this, R.color.black));

            TextView tvSubtotal = new TextView(this);
            tvSubtotal.setText(String.format(Locale.getDefault(), "%,d đ", item.getSubtotal()));
            tvSubtotal.setTextSize(13f);
            tvSubtotal.setTypeface(null, Typeface.BOLD);
            tvSubtotal.setTextColor(ContextCompat.getColor(this, R.color.black));

            row.addView(tvItemName);
            row.addView(tvSubtotal);
            dialogBinding.layoutOrderItemsList.addView(row);
        }

        dialogBinding.tvDetailTotalAmount.setText(order.getFormattedTotal());

        dialogBinding.btnPrintReceipt.setOnClickListener(v -> {
            Toast.makeText(
                    AdminActivity.this,
                    "🖨️ Đã xuất lệnh in hóa đơn " + order.getOrderId() + " (" + order.getTableNumber() + " - " + order.getFormattedTotal() + ")!",
                    Toast.LENGTH_LONG
            ).show();
        });

        dialogBinding.btnUpdateStatusFromDetail.setOnClickListener(v -> {
            dialog.dismiss();
            showChangeOrderStatusDialog(order);
        });

        dialogBinding.btnCloseOrderDetail.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * UC10: Xem lịch sử đơn hàng & hạng thành viên VIP của một khách hàng cụ thể
     */
    private void showCustomerOrderHistoryPopup(Customer customer) {
        List<Order> orders = orderService.getCustomerOrders(customer.getId());
        LoyaltyProfile loyalty = opsService.getCustomerLoyaltyProfile(customer.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("🏅 Hạng thành viên: ").append(loyalty.getTierName())
                .append(" (Tích lũy ").append(loyalty.getPoints()).append(" điểm)\n");
        sb.append("📞 Số điện thoại: ").append(customer.getPhoneNumber()).append("\n");
        sb.append("✉️ Email: ").append(customer.getEmail() != null && !customer.getEmail().isEmpty() ? customer.getEmail() : "Chưa cập nhật").append("\n");
        if (customer.getAddress() != null && !customer.getAddress().isEmpty()) {
            sb.append("📍 Địa chỉ / Bàn: ").append(customer.getAddress()).append("\n");
        }
        sb.append("\n────────────────────────\n");
        if (orders.isEmpty()) {
            sb.append("Khách hàng này chưa có đơn hàng nào.");
        } else {
            sb.append("Lịch sử đặt món (").append(orders.size()).append(" đơn):\n\n");
            for (Order o : orders) {
                sb.append("• ").append(o.getOrderId())
                        .append(" (").append(o.getTableNumber()).append(") - ")
                        .append(o.getFormattedTotal())
                        .append(" [").append(o.getStatus().getDisplayName()).append("]\n")
                        .append("   Món: ").append(o.getItemsSummary()).append("\n\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("👤 Hồ Sơ Khách Hàng: " + customer.getFullName())
                .setMessage(sb.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showAddEditFoodDialog(MenuItem food) {
        DialogAddEditFoodBinding dialogBinding = DialogAddEditFoodBinding.inflate(LayoutInflater.from(this));
        VietnameseTelexHelper.attachToEditText(dialogBinding.etFoodName, null);
        VietnameseTelexHelper.attachToEditText(dialogBinding.etFoodDesc, null);
        boolean isEditing = food != null;

        if (isEditing) {
            dialogBinding.tvDialogTitle.setText("📝 Chỉnh Sửa Món Ăn");
            dialogBinding.etFoodName.setText(food.getName());
            dialogBinding.etFoodPrice.setText(String.valueOf(food.getPrice()));
            dialogBinding.etFoodDesc.setText(food.getDescription());

            if (food.getCategory() == FoodCategory.MAIN_DISH) {
                dialogBinding.chipAddMain.setChecked(true);
            } else if (food.getCategory() == FoodCategory.SIDE_DISH) {
                dialogBinding.chipAddSide.setChecked(true);
            } else if (food.getCategory() == FoodCategory.BEVERAGE) {
                dialogBinding.chipAddDrink.setChecked(true);
            }

            List<TasteAttribute> existingTastes = food.getTasteAttributes();
            dialogBinding.chipTasteSavory.setChecked(existingTastes.contains(TasteAttribute.SAVORY));
            dialogBinding.chipTasteCrispy.setChecked(existingTastes.contains(TasteAttribute.CRISPY));
            dialogBinding.chipTasteSpicy.setChecked(existingTastes.contains(TasteAttribute.SPICY));
            dialogBinding.chipTasteSweet.setChecked(existingTastes.contains(TasteAttribute.SWEET));
            dialogBinding.chipTasteSour.setChecked(existingTastes.contains(TasteAttribute.SOUR));
            dialogBinding.chipTasteRefreshing.setChecked(existingTastes.contains(TasteAttribute.REFRESHING));
            dialogBinding.chipTasteGrilled.setChecked(existingTastes.contains(TasteAttribute.GRILLED));
            dialogBinding.chipTasteFried.setChecked(existingTastes.contains(TasteAttribute.FRIED));
        } else {
            dialogBinding.tvDialogTitle.setText("➕ Thêm Món Ăn Mới");
        }

        final String[] selectedImagePathHolder = new String[]{isEditing ? food.getImagePath() : ""};

        Runnable refreshImagePreview = () -> {
            String customPath = selectedImagePathHolder[0];
            FoodCategory previewCat = resolveDialogCategory(dialogBinding);
            String typedName = dialogBinding.etFoodName.getText() != null
                    ? dialogBinding.etFoodName.getText().toString().trim()
                    : "";
            int fallbackResId = isEditing
                    ? (food.getImageResId() != 0 ? food.getImageResId() : FoodImageResolver.resolveByName(typedName.isEmpty() ? food.getName() : typedName, previewCat))
                    : FoodImageResolver.resolveByName(typedName, previewCat);

            FoodImageLoader.loadImagePathOrFallback(dialogBinding.imgFoodPreview, customPath, fallbackResId);

            boolean hasCustom = customPath != null && !customPath.trim().isEmpty();
            if (hasCustom) {
                dialogBinding.tvSelectedImageStatus.setText("✅ Đã chọn ảnh từ điện thoại");
                dialogBinding.tvSelectedImageStatus.setTextColor(ContextCompat.getColor(this, R.color.admin_status_complete));
                dialogBinding.btnResetFoodImage.setVisibility(View.VISIBLE);
            } else {
                dialogBinding.tvSelectedImageStatus.setText("🖼️ Đang dùng ảnh mặc định theo tên món");
                dialogBinding.tvSelectedImageStatus.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
                dialogBinding.btnResetFoodImage.setVisibility(View.GONE);
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        refreshImagePreview.run();

        dialogBinding.etFoodName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selectedImagePathHolder[0] == null || selectedImagePathHolder[0].trim().isEmpty()) {
                    refreshImagePreview.run();
                }
            }
        });

        dialogBinding.chipGroupAddCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (selectedImagePathHolder[0] == null || selectedImagePathHolder[0].trim().isEmpty()) {
                refreshImagePreview.run();
            }
        });

        dialogBinding.btnPickFoodImage.setOnClickListener(v -> {
            activeImagePickedCallback = savedPath -> {
                selectedImagePathHolder[0] = savedPath;
                refreshImagePreview.run();
                Toast.makeText(AdminActivity.this, "Đã tải ảnh món ăn lên!", Toast.LENGTH_SHORT).show();
            };
            pickFoodImageLauncher.launch("image/*");
        });

        dialogBinding.btnResetFoodImage.setOnClickListener(v -> {
            selectedImagePathHolder[0] = "";
            refreshImagePreview.run();
            Toast.makeText(AdminActivity.this, "Đã chuyển về ảnh mặc định", Toast.LENGTH_SHORT).show();
        });

        dialog.setOnDismissListener(d -> activeImagePickedCallback = null);

        dialogBinding.btnSaveFood.setOnClickListener(v -> {
            String name = dialogBinding.etFoodName.getText() != null
                    ? dialogBinding.etFoodName.getText().toString().trim()
                    : "";
            String priceStr = dialogBinding.etFoodPrice.getText() != null
                    ? dialogBinding.etFoodPrice.getText().toString().trim()
                    : "";
            String desc = dialogBinding.etFoodDesc.getText() != null
                    ? dialogBinding.etFoodDesc.getText().toString().trim()
                    : "";

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Vui lòng nhập tên món và giá bán!", Toast.LENGTH_SHORT).show();
                return;
            }

            long price;
            try {
                price = Long.parseLong(priceStr);
            } catch (NumberFormatException e) {
                price = 0L;
            }

            if (price <= 0) {
                Toast.makeText(AdminActivity.this, "Giá bán không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            FoodCategory category = resolveDialogCategory(dialogBinding);

            List<TasteAttribute> selectedTastes = new ArrayList<>();
            if (dialogBinding.chipTasteSavory.isChecked()) selectedTastes.add(TasteAttribute.SAVORY);
            if (dialogBinding.chipTasteCrispy.isChecked()) selectedTastes.add(TasteAttribute.CRISPY);
            if (dialogBinding.chipTasteSpicy.isChecked()) selectedTastes.add(TasteAttribute.SPICY);
            if (dialogBinding.chipTasteSweet.isChecked()) selectedTastes.add(TasteAttribute.SWEET);
            if (dialogBinding.chipTasteSour.isChecked()) selectedTastes.add(TasteAttribute.SOUR);
            if (dialogBinding.chipTasteRefreshing.isChecked()) selectedTastes.add(TasteAttribute.REFRESHING);
            if (dialogBinding.chipTasteGrilled.isChecked()) selectedTastes.add(TasteAttribute.GRILLED);
            if (dialogBinding.chipTasteFried.isChecked()) selectedTastes.add(TasteAttribute.FRIED);
            if (selectedTastes.isEmpty()) {
                selectedTastes.add(TasteAttribute.SAVORY);
            }

            String chosenImagePath = selectedImagePathHolder[0] != null ? selectedImagePathHolder[0].trim() : "";

            if (isEditing) {
                MenuItem updated = new MenuItem(
                        food.getId(),
                        name,
                        desc,
                        price,
                        category,
                        selectedTastes,
                        food.isAvailable(),
                        food.getRating(),
                        food.getOrderCount(),
                        food.getImageResId(),
                        chosenImagePath
                );
                menuService.updateMenuItem(updated);
                Toast.makeText(AdminActivity.this, "Đã cập nhật món '" + updated.getName() + "'!", Toast.LENGTH_SHORT).show();
            } else {
                menuService.createMenuItem(
                        name,
                        desc,
                        price,
                        category,
                        selectedTastes,
                        chosenImagePath
                );
                Toast.makeText(AdminActivity.this, "Đã thêm món mới thành công!", Toast.LENGTH_SHORT).show();
            }

            refreshAdminProductsList();
            refreshDashboardMetrics();
            dialog.dismiss();
        });

        dialog.show();
    }

    private FoodCategory resolveDialogCategory(DialogAddEditFoodBinding dialogBinding) {
        int checkedId = dialogBinding.chipGroupAddCategory.getCheckedChipId();
        if (checkedId == R.id.chipAddSide) {
            return FoodCategory.SIDE_DISH;
        }
        if (checkedId == R.id.chipAddDrink) {
            return FoodCategory.BEVERAGE;
        }
        return FoodCategory.MAIN_DISH;
    }

    private void confirmDeleteFood(MenuItem food) {
        new AlertDialog.Builder(this)
                .setTitle("Xác Nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa món '" + food.getName() + "' khỏi thực đơn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = menuService.deleteMenuItem(food.getId());
                    refreshAdminProductsList();
                    refreshDashboardMetrics();
                    if (deleted) {
                        Toast.makeText(AdminActivity.this, "Đã cập nhật thực đơn (món đã có trong đơn được ẩn để bảo vệ lịch sử)!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminActivity.this, "Không thể xóa món ăn!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangeOrderStatusDialog(Order order) {
        String[] statusList = new String[]{
                OrderStatus.PENDING.getDisplayName(),
                OrderStatus.PREPARING.getDisplayName(),
                OrderStatus.COMPLETED.getDisplayName(),
                OrderStatus.CANCELLED.getDisplayName()
        };

        new AlertDialog.Builder(this)
                .setTitle("Cập Nhật Trạng Thái Đơn " + order.getOrderId())
                .setItems(statusList, (dialog, which) -> {
                    OrderStatus newStatus;
                    switch (which) {
                        case 1:
                            newStatus = OrderStatus.PREPARING;
                            break;
                        case 2:
                            newStatus = OrderStatus.COMPLETED;
                            break;
                        case 3:
                            newStatus = OrderStatus.CANCELLED;
                            break;
                        case 0:
                        default:
                            newStatus = OrderStatus.PENDING;
                            break;
                    }

                    boolean success = orderService.updateOrderStatus(order.getOrderId(), newStatus);
                    if (success) {
                        refreshAdminOrdersList();
                        refreshDashboardMetrics();
                        Toast.makeText(AdminActivity.this, "Đã chuyển đơn " + order.getOrderId() + " sang '" + newStatus.getDisplayName() + "'", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                AdminActivity.this,
                                "Không thể chuyển trạng thái từ '" + order.getStatus().getDisplayName() + "' sang '" + newStatus.getDisplayName() + "'!",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}
