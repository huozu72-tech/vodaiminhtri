package com.example.chieuthu3_ketthucmonhoc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chieuthu3_ketthucmonhoc.adapters.AdminFoodAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.AdminOrderAdapter;
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityAdminBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAddEditFoodBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardMetrics;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IDashboardService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;

import java.util.Collections;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private AdminFoodAdapter adminFoodAdapter;
    private AdminOrderAdapter adminOrderAdapter;

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
            Toast.makeText(this, "Bạn cần đăng nhập quản trị để truy cập màn hình này!", Toast.LENGTH_LONG).show()
;
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
        setupListeners();
        refreshDashboardMetrics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardMetrics();
        adminFoodAdapter.updateList(menuService.getAllMenuItems());
        adminOrderAdapter.updateList(orderService.getAllOrders());
    }

    private void setupTabs() {
        binding.chipGroupAdminTabs.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            binding.layoutTabDashboard.setVisibility(View.GONE);
            binding.layoutTabProducts.setVisibility(View.GONE);
            binding.layoutTabOrders.setVisibility(View.GONE);

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipTabProducts) {
                binding.layoutTabProducts.setVisibility(View.VISIBLE);
                adminFoodAdapter.updateList(menuService.getAllMenuItems());
            } else if (checkedId == R.id.chipTabOrders) {
                binding.layoutTabOrders.setVisibility(View.VISIBLE);
                adminOrderAdapter.updateList(orderService.getAllOrders());
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
        binding.tvStatCompletionRate.setText("Tỷ Lệ Hoàn Thành Đơn: " + metrics.getCompletionRatePercent() + "% 🎯");

        AdminAccount currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin != null) {
            binding.tvAdminGreeting.setText(currentAdmin.getFullName());
        } else {
            binding.tvAdminGreeting.setText("Quản Trị Viên");
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
                }
        );
        binding.rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminProducts.setAdapter(adminFoodAdapter);
    }

    private void setupOrderRecyclerView() {
        adminOrderAdapter = new AdminOrderAdapter(
                orderService.getAllOrders(),
                this::showChangeOrderStatusDialog
        );
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminOrders.setAdapter(adminOrderAdapter);
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

            adminFoodAdapter.updateList(menuService.getAllMenuItems());
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
                    adminFoodAdapter.updateList(menuService.getAllMenuItems());
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
                        adminOrderAdapter.updateList(orderService.getAllOrders());
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
