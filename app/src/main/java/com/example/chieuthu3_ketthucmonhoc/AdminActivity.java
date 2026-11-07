package com.example.chieuthu3_ketthucmonhoc;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardMetrics;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IDashboardService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.TopSellingItem;

import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize services
        authService = AppServiceLocator.getAuthService();
        menuService = AppServiceLocator.getMenuService();
        orderService = AppServiceLocator.getOrderService();
        dashboardService = AppServiceLocator.getDashboardService();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardMetrics();
        refreshAdminProductsList();
        refreshAdminOrdersList();
        refreshAdminCustomersList();
    }

    private void setupTabs() {
        binding.chipGroupAdminTabs.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            binding.layoutTabDashboard.setVisibility(View.GONE);
            binding.layoutTabProducts.setVisibility(View.GONE);
            binding.layoutTabOrders.setVisibility(View.GONE);
            binding.layoutTabCustomers.setVisibility(View.GONE);

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipTabProducts) {
                binding.layoutTabProducts.setVisibility(View.VISIBLE);
                refreshAdminProductsList();
            } else if (checkedId == R.id.chipTabOrders) {
                binding.layoutTabOrders.setVisibility(View.VISIBLE);
                refreshAdminOrdersList();
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
                tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
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
            tvSubtotal.setTypeface(null, android.graphics.Typeface.BOLD);
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
     * UC10: Xem lịch sử đơn hàng của một khách hàng cụ thể từ Tab Quản Lý Khách Hàng
     */
    private void showCustomerOrderHistoryPopup(Customer customer) {
        List<Order> orders = orderService.getCustomerOrders(customer.getId());
        StringBuilder sb = new StringBuilder();
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
        } else {
            dialogBinding.tvDialogTitle.setText("➕ Thêm Món Ăn Mới");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

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

            FoodCategory category;
            int checkedId = dialogBinding.chipGroupAddCategory.getCheckedChipId();
            if (checkedId == R.id.chipAddSide) {
                category = FoodCategory.SIDE_DISH;
            } else if (checkedId == R.id.chipAddDrink) {
                category = FoodCategory.BEVERAGE;
            } else {
                category = FoodCategory.MAIN_DISH;
            }

            if (isEditing) {
                MenuItem updated = new MenuItem(
                        food.getId(),
                        name,
                        desc,
                        price,
                        category,
                        food.getTasteAttributes(),
                        food.isAvailable(),
                        food.getRating(),
                        food.getOrderCount(),
                        food.getImageResId()
                );
                menuService.updateMenuItem(updated);
                Toast.makeText(AdminActivity.this, "Đã cập nhật món '" + updated.getName() + "'!", Toast.LENGTH_SHORT).show();
            } else {
                menuService.createMenuItem(
                        name,
                        desc,
                        price,
                        category,
                        Collections.singletonList(TasteAttribute.SAVORY)
                );
                Toast.makeText(AdminActivity.this, "Đã thêm món mới thành công!", Toast.LENGTH_SHORT).show();
            }

            refreshAdminProductsList();
            refreshDashboardMetrics();
            dialog.dismiss();
        });

        dialog.show();
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
