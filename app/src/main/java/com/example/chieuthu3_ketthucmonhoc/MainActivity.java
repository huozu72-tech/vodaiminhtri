package com.example.chieuthu3_ketthucmonhoc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chieuthu3_ketthucmonhoc.adapters.AIRecommendationAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.CartAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.ChatAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.FoodAdapter;
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityMainBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAiChatBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCartBottomSheetBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCustomerProfileBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogFoodDetailBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogLoginBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogPaymentQrBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIChatbotService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIRecommendationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.ICartService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.QrBitmapGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FoodAdapter foodAdapter;
    private AIRecommendationAdapter aiRecAdapter;

    // Services via AppServiceLocator (Clean OOP Architecture)
    private IMenuService menuService;
    private ICartService cartService;
    private IOrderService orderService;
    private IAuthenticationService authService;
    private IAIRecommendationService aiRecService;
    private IAIChatbotService aiChatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize services
        menuService = AppServiceLocator.getMenuService();
        cartService = AppServiceLocator.getCartService();
        orderService = AppServiceLocator.getOrderService();
        authService = AppServiceLocator.getAuthService();
        aiRecService = AppServiceLocator.getAiRecommendationService();
        aiChatService = AppServiceLocator.getAiChatbotService();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupFoodGrid();
        setupAiRecommendations();
        setupSearchAndFilters();
        setupListeners();
        updateCartBadge();
        updateUserHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFoodList();
        refreshAiRecommendations();
        updateCartBadge();
        updateUserHeader();
    }

    private void setupFoodGrid() {
        foodAdapter = new FoodAdapter(
                menuService.getAvailableMenuItems(),
                item -> {
                    cartService.addItem(item, 1);
                    updateCartBadge();
                    refreshAiRecommendations();
                    Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
                },
                this::showFoodDetailDialog
        );
        binding.rvFoodGrid.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvFoodGrid.setAdapter(foodAdapter);
    }

    private void setupAiRecommendations() {
        Customer currentCustomer = authService.getCurrentCustomer();
        Integer customerId = currentCustomer != null ? currentCustomer.getId() : null;
        aiRecAdapter = new AIRecommendationAdapter(
                aiRecService.getPersonalizedRecommendations(customerId, 5),
                item -> {
                    cartService.addItem(item, 1);
                    updateCartBadge();
                    Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
                },
                this::showFoodDetailDialog
        );
        binding.rvAiRecommendations.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvAiRecommendations.setAdapter(aiRecAdapter);
    }

    private void refreshFoodList() {
        int checkedId = binding.chipGroupCategory.getCheckedChipId();
        FoodCategory currentCategory = null;
        if (checkedId == R.id.chipMain) {
            currentCategory = FoodCategory.MAIN_DISH;
        } else if (checkedId == R.id.chipSide) {
            currentCategory = FoodCategory.SIDE_DISH;
        } else if (checkedId == R.id.chipDrink) {
            currentCategory = FoodCategory.BEVERAGE;
        }

        List<MenuItem> items;
        if (currentCategory != null) {
            items = menuService.getMenuItemsByCategory(currentCategory);
        } else {
            items = menuService.getAvailableMenuItems();
        }
        foodAdapter.updateList(items);
    }

    private void refreshAiRecommendations() {
        Customer currentCustomer = authService.getCurrentCustomer();
        Integer customerId = currentCustomer != null ? currentCustomer.getId() : null;
        aiRecAdapter.updateList(aiRecService.getPersonalizedRecommendations(customerId, 5));
    }

    private void setupSearchAndFilters() {
        binding.etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (query.isEmpty()) {
                    refreshFoodList();
                } else {
                    List<MenuItem> searchResults = menuService.searchMenuItems(query);
                    List<MenuItem> availableOnly = new ArrayList<>();
                    for (MenuItem item : searchResults) {
                        if (item.isAvailable()) {
                            availableOnly.add(item);
                        }
                    }
                    foodAdapter.updateList(availableOnly);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            if (binding.etSearchFood.getText() != null) {
                binding.etSearchFood.getText().clear();
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipMain) {
                foodAdapter.updateList(menuService.getMenuItemsByCategory(FoodCategory.MAIN_DISH));
            } else if (checkedId == R.id.chipSide) {
                foodAdapter.updateList(menuService.getMenuItemsByCategory(FoodCategory.SIDE_DISH));
            } else if (checkedId == R.id.chipDrink) {
                foodAdapter.updateList(menuService.getMenuItemsByCategory(FoodCategory.BEVERAGE));
            } else {
                foodAdapter.updateList(menuService.getAvailableMenuItems());
            }
        });
    }

    private void setupListeners() {
        binding.btnHeaderCart.setOnClickListener(v -> showCartBottomSheet());
        binding.btnHeaderUser.setOnClickListener(v -> handleUserClick());

        binding.btnAiUploadPhoto.setOnClickListener(v -> showImageRecognitionDialog());
        binding.fabAiChatbot.setOnClickListener(v -> showAiChatDialog());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                binding.chipAll.setChecked(true);
                foodAdapter.updateList(menuService.getAvailableMenuItems());
                return true;
            } else if (itemId == R.id.nav_ai) {
                showAiChatDialog();
                return true;
            } else if (itemId == R.id.nav_cart) {
                showCartBottomSheet();
                return true;
            } else if (itemId == R.id.nav_payment) {
                showPaymentDialog();
                return true;
            } else if (itemId == R.id.nav_profile) {
                handleUserClick();
                return true;
            }
            return false;
        });
    }

    private void updateCartBadge() {
        int count = cartService.getTotalItemCount();
        binding.tvHeaderCartCount.setText(String.format(Locale.getDefault(), "Giỏ (%d)", count));
    }

    private void updateUserHeader() {
        Customer customer = authService.getCurrentCustomer();
        AdminAccount admin = authService.getCurrentAdmin();
        if (customer != null) {
            binding.tvHeaderUserName.setText(customer.getFullName());
        } else if (admin != null) {
            binding.tvHeaderUserName.setText("Admin: " + admin.getFullName());
        } else {
            binding.tvHeaderUserName.setText("Đăng Nhập");
        }
    }

    /**
     * Xem chi tiết món ăn (Hình 10 Activity Diagram) + Đánh giá món ăn (UC-07) + Món tương tự AI
     */
    private void showFoodDetailDialog(MenuItem initialItem) {
        MenuItem latest = menuService.getMenuItemById(initialItem.getId());
        final MenuItem[] dishHolder = new MenuItem[]{latest != null ? latest : initialItem};

        DialogFoodDetailBinding dialogBinding = DialogFoodDetailBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        final int[] quantityHolder = new int[]{1};

        Runnable bindDishData = () -> {
            MenuItem dish = dishHolder[0];
            dialogBinding.imgDetailFood.setImageResource(FoodImageResolver.resolveImage(dish));
            dialogBinding.tvDetailCategoryBadge.setText(dish.getCategory().getDisplayName());
            dialogBinding.tvDetailFoodName.setText(dish.getName());
            dialogBinding.tvDetailFoodPrice.setText(dish.getFormattedPrice());
            dialogBinding.tvDetailRatingSold.setText(
                    String.format(Locale.US, "⭐ %.1f / 5.0 • 🔥 Đã phục vụ %d phần", dish.getRating(), dish.getOrderCount())
            );
            dialogBinding.tvDetailFoodDesc.setText(dish.getDescription());

            List<TasteAttribute> tastes = dish.getTasteAttributes();
            if (tastes != null && !tastes.isEmpty()) {
                StringBuilder sb = new StringBuilder("🏷️ Hương vị đặc trưng: ");
                for (int i = 0; i < tastes.size(); i++) {
                    if (i > 0) sb.append(" • ");
                    sb.append(tastes.get(i).getDisplayName());
                }
                dialogBinding.tvDetailTasteTags.setText(sb.toString());
                dialogBinding.tvDetailTasteTags.setVisibility(View.VISIBLE);
            } else {
                dialogBinding.tvDetailTasteTags.setVisibility(View.GONE);
            }

            long subtotal = dish.getPrice() * quantityHolder[0];
            dialogBinding.tvDetailQuantity.setText(String.valueOf(quantityHolder[0]));
            dialogBinding.btnDetailAddToCart.setText(
                    String.format(Locale.getDefault(), "🛒 Thêm Vào Giỏ • %,d đ", subtotal)
            );
        };

        bindDishData.run();

        dialogBinding.btnCloseDetail.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnDetailMinus.setOnClickListener(v -> {
            if (quantityHolder[0] > 1) {
                quantityHolder[0]--;
                bindDishData.run();
            }
        });

        dialogBinding.btnDetailPlus.setOnClickListener(v -> {
            if (quantityHolder[0] < 20) {
                quantityHolder[0]++;
                bindDishData.run();
            }
        });

        dialogBinding.btnDetailAddToCart.setOnClickListener(v -> {
            MenuItem dish = dishHolder[0];
            cartService.addItem(dish, quantityHolder[0]);
            updateCartBadge();
            refreshAiRecommendations();
            dialog.dismiss();
            Toast.makeText(
                    MainActivity.this,
                    "Đã thêm " + quantityHolder[0] + "x '" + dish.getName() + "' vào giỏ hàng!",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // UC-07: Đánh giá món ăn (1 - 5 sao)
        View.OnClickListener rateClickListener = v -> {
            int stars = 5;
            int id = v.getId();
            if (id == R.id.btnRate1) stars = 1;
            else if (id == R.id.btnRate2) stars = 2;
            else if (id == R.id.btnRate3) stars = 3;
            else if (id == R.id.btnRate4) stars = 4;

            MenuItem updated = menuService.rateMenuItem(dishHolder[0].getId(), stars);
            if (updated != null) {
                dishHolder[0] = updated;
                bindDishData.run();
                refreshFoodList();
                refreshAiRecommendations();
                dialogBinding.tvRatingFeedback.setText(
                        String.format(Locale.US, "✅ Cảm ơn bạn đã chấm %d★! Điểm hiện tại: ⭐ %.1f/5.0", stars, updated.getRating())
                );
            }
        };
        dialogBinding.btnRate1.setOnClickListener(rateClickListener);
        dialogBinding.btnRate2.setOnClickListener(rateClickListener);
        dialogBinding.btnRate3.setOnClickListener(rateClickListener);
        dialogBinding.btnRate4.setOnClickListener(rateClickListener);
        dialogBinding.btnRate5.setOnClickListener(rateClickListener);

        // AI Similar Dishes (Hình 10: Món tương tự từ Hệ thống AI)
        List<AIRecommendation> similarDishes = aiRecService.getSimilarDishes(dishHolder[0].getId(), 3);
        dialogBinding.layoutSimilarDishesContainer.removeAllViews();
        for (AIRecommendation rec : similarDishes) {
            MenuItem simItem = rec.getMenuItem();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(20, 14, 20, 14);
            row.setBackgroundResource(R.drawable.bg_card_food);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 6, 0, 6);
            row.setLayoutParams(rowParams);

            TextView tvInfo = new TextView(this);
            tvInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvInfo.setText(simItem.getName() + " • " + simItem.getFormattedPrice() + "\n" + rec.getReason());
            tvInfo.setTextSize(12f);
            tvInfo.setTextColor(ContextCompat.getColor(this, R.color.black));

            MaterialButton btnQuickAdd = new MaterialButton(this);
            btnQuickAdd.setText("+ Giỏ");
            btnQuickAdd.setTextSize(10f);
            btnQuickAdd.setAllCaps(false);
            btnQuickAdd.setOnClickListener(v -> {
                cartService.addItem(simItem, 1);
                updateCartBadge();
                refreshAiRecommendations();
                Toast.makeText(MainActivity.this, "Đã thêm '" + simItem.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
            });

            row.setOnClickListener(v -> {
                dialog.dismiss();
                showFoodDetailDialog(simItem);
            });

            row.addView(tvInfo);
            row.addView(btnQuickAdd);
            dialogBinding.layoutSimilarDishesContainer.addView(row);
        }

        dialog.show();
    }

    private void handleUserClick() {
        Customer customer = authService.getCurrentCustomer();
        AdminAccount admin = authService.getCurrentAdmin();
        if (customer != null) {
            showCustomerProfileDialog(customer);
        } else if (admin != null) {
            new AlertDialog.Builder(this)
                    .setTitle("🛠️ Phiên Quản Trị: " + admin.getFullName())
                    .setMessage("Bạn đang đăng nhập với quyền quản trị viên (" + admin.getRole().getDisplayName() + ").")
                    .setPositiveButton("Vào Quản Trị", (dialog, which) -> {
                        startActivity(new Intent(MainActivity.this, AdminActivity.class));
                    })
                    .setNegativeButton("Đăng Xuất", (dialog, which) -> {
                        authService.logout();
                        updateUserHeader();
                        Toast.makeText(MainActivity.this, "Đã đăng xuất phiên quản trị!", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Đóng", null)
                    .show();
        } else {
            showLoginDialog();
        }
    }

    /**
     * Hình 24: Giao diện Hồ Sơ Khách Hàng (Xem & Cập nhật thông tin) + UC-06: Theo dõi & Hủy đơn hàng
     */
    private void showCustomerProfileDialog(Customer initialCustomer) {
        DialogCustomerProfileBinding dialogBinding = DialogCustomerProfileBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        final Customer[] customerHolder = new Customer[]{initialCustomer};

        Runnable bindProfileHeaderAndForm = () -> {
            Customer c = customerHolder[0];
            dialogBinding.tvProfileFullName.setText(c.getFullName());
            dialogBinding.tvProfilePhone.setText("Số điện thoại: " + c.getPhoneNumber());
            dialogBinding.tvProfileEmail.setText(
                    c.getEmail() != null && !c.getEmail().trim().isEmpty()
                            ? "Email: " + c.getEmail()
                            : "Email: Chưa cập nhật"
            );
            dialogBinding.etProfileFullName.setText(c.getFullName());
            dialogBinding.etProfileEmail.setText(c.getEmail() != null ? c.getEmail() : "");
            dialogBinding.etProfileAddress.setText(c.getAddress() != null ? c.getAddress() : "");
            dialogBinding.etProfileNewPassword.setText("");
        };

        Runnable bindOrderHistory = new Runnable() {
            @Override
            public void run() {
                dialogBinding.layoutCustomerOrders.removeAllViews();
                List<Order> orders = orderService.getCustomerOrders(customerHolder[0].getId());
                if (orders.isEmpty()) {
                    TextView emptyView = new TextView(MainActivity.this);
                    emptyView.setText("Bạn chưa có đơn hàng nào.");
                    emptyView.setGravity(Gravity.CENTER);
                    emptyView.setPadding(16, 24, 16, 24);
                    dialogBinding.layoutCustomerOrders.addView(emptyView);
                    return;
                }

                int displayCount = Math.min(orders.size(), 8);
                for (int i = 0; i < displayCount; i++) {
                    Order order = orders.get(i);
                    LinearLayout card = new LinearLayout(MainActivity.this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(24, 18, 24, 18);
                    card.setBackgroundResource(R.drawable.bg_card_food);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 8, 0, 8);
                    card.setLayoutParams(params);

                    TextView tvOrderHeader = new TextView(MainActivity.this);
                    tvOrderHeader.setText(
                            order.getOrderId() + " • " + order.getTableNumber() + " • " + order.getStatus().getDisplayName()
                    );
                    tvOrderHeader.setTextSize(13f);
                    tvOrderHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                    try {
                        tvOrderHeader.setTextColor(Color.parseColor(order.getStatus().getColorHex()));
                    } catch (Exception e) {
                        tvOrderHeader.setTextColor(Color.BLACK);
                    }

                    TextView tvOrderDetails = new TextView(MainActivity.this);
                    tvOrderDetails.setText(
                            "Thời gian: " + order.getFormattedTime() + " • " + order.getPaymentMethod()
                                    + "\nMón: " + order.getItemsSummary()
                                    + "\nTổng thanh toán: " + order.getFormattedTotal()
                    );
                    tvOrderDetails.setTextSize(12f);
                    tvOrderDetails.setPadding(0, 6, 0, 6);
                    tvOrderDetails.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));

                    card.addView(tvOrderHeader);
                    card.addView(tvOrderDetails);

                    // Nút hủy đơn nếu đơn đang Chờ xác nhận (PENDING) - UC-06 Theo dõi đơn hàng
                    if (order.getStatus() == OrderStatus.PENDING) {
                        MaterialButton btnCancelOrder = new MaterialButton(MainActivity.this);
                        btnCancelOrder.setText("❌ Hủy Đơn Hàng Này");
                        btnCancelOrder.setAllCaps(false);
                        btnCancelOrder.setTextSize(11f);
                        btnCancelOrder.setBackgroundColor(Color.parseColor("#FEE2E2"));
                        btnCancelOrder.setTextColor(Color.parseColor("#DC2626"));
                        btnCancelOrder.setOnClickListener(v -> {
                            boolean cancelled = orderService.cancelOrder(order.getOrderId());
                            if (cancelled) {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Đã hủy đơn hàng " + order.getOrderId() + " thành công!",
                                        Toast.LENGTH_SHORT
                                ).show();
                                this.run();
                            }
                        });
                        card.addView(btnCancelOrder);
                    }

                    dialogBinding.layoutCustomerOrders.addView(card);
                }
            }
        };

        bindProfileHeaderAndForm.run();
        bindOrderHistory.run();

        dialogBinding.btnSaveProfile.setOnClickListener(v -> {
            String newName = dialogBinding.etProfileFullName.getText().toString();
            String newEmail = dialogBinding.etProfileEmail.getText().toString();
            String newAddress = dialogBinding.etProfileAddress.getText().toString();
            String newPass = dialogBinding.etProfileNewPassword.getText().toString();

            AuthResult<Customer> res = authService.updateProfile(
                    customerHolder[0].getId(),
                    newName,
                    newEmail,
                    newAddress,
                    newPass
            );
            if (res.isSuccess()) {
                customerHolder[0] = res.getData();
                bindProfileHeaderAndForm.run();
                updateUserHeader();
                Toast.makeText(MainActivity.this, "Đã cập nhật hồ sơ cá nhân thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialogBinding.btnLogoutCustomer.setOnClickListener(v -> {
            authService.logout();
            updateUserHeader();
            refreshAiRecommendations();
            dialog.dismiss();
            Toast.makeText(MainActivity.this, "Đã đăng xuất tài khoản!", Toast.LENGTH_SHORT).show();
        });

        dialogBinding.btnCloseProfile.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCartBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        DialogCartBottomSheetBinding dialogBinding = DialogCartBottomSheetBinding.inflate(LayoutInflater.from(this));
        bottomSheet.setContentView(dialogBinding.getRoot());

        final CartAdapter[] cartAdapterHolder = new CartAdapter[1];
        cartAdapterHolder[0] = new CartAdapter(
                cartService.getCartItems(),
                new CartAdapter.OnCartItemActionListener() {
                    @Override
                    public void onQuantityPlus(CartItem item) {
                        cartService.updateQuantity(item.getMenuItem().getId(), item.getQuantity() + 1);
                        cartAdapterHolder[0].updateList(cartService.getCartItems());
                        updateCartBadge();
                        calculateCartTotal(dialogBinding);
                    }

                    @Override
                    public void onQuantityMinus(CartItem item) {
                        cartService.updateQuantity(item.getMenuItem().getId(), item.getQuantity() - 1);
                        cartAdapterHolder[0].updateList(cartService.getCartItems());
                        updateCartBadge();
                        calculateCartTotal(dialogBinding);
                    }
                }
        );

        dialogBinding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.rvCartItems.setAdapter(cartAdapterHolder[0]);

        calculateCartTotal(dialogBinding);

        dialogBinding.tvClearCart.setOnClickListener(v -> {
            cartService.clearCart();
            cartAdapterHolder[0].updateList(new ArrayList<>());
            updateCartBadge();
            calculateCartTotal(dialogBinding);
            Toast.makeText(MainActivity.this, "Đã xóa toàn bộ giỏ hàng!", Toast.LENGTH_SHORT).show();
        });

        dialogBinding.btnCheckout.setOnClickListener(v -> {
            if (cartService.isEmpty()) {
                Toast.makeText(MainActivity.this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                bottomSheet.dismiss();
                showPaymentDialog();
            }
        });

        bottomSheet.show();
    }

    private void calculateCartTotal(DialogCartBottomSheetBinding dialogBinding) {
        long total = cartService.getTotalAmount();
        dialogBinding.tvCartTotalPrice.setText(String.format(Locale.getDefault(), "%,d đ", total));
    }

    private void showPaymentDialog() {
        if (cartService.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống! Vui lòng chọn món trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        long totalAmount = cartService.getTotalAmount();
        int itemCount = cartService.getTotalItemCount();
        DialogPaymentQrBinding dialogBinding = DialogPaymentQrBinding.inflate(LayoutInflater.from(this));

        dialogBinding.etTableNumber.setText("12");

        java.util.concurrent.Callable<String> getSelectedPaymentMethod = () -> {
            int checkedId = dialogBinding.chipGroupPaymentMethod.getCheckedChipId();
            if (checkedId == R.id.chipPayCash) {
                return "Tiền mặt tại bàn";
            } else if (checkedId == R.id.chipPayWallet) {
                return "Ví MoMo / ZaloPay";
            }
            return "Chuyển khoản QR";
        };

        Runnable updateSummary = () -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "";
            String tableLabel = !tableRaw.isEmpty() ? "Bàn " + tableRaw : "Bàn ?";
            String method = "Chuyển khoản QR";
            try {
                method = getSelectedPaymentMethod.call();
            } catch (Exception ignored) {}

            dialogBinding.tvOrderSummary.setText(String.format(
                    Locale.getDefault(),
                    "Hóa đơn %s • Tổng: %,d đ (%d món) • %s",
                    tableLabel,
                    totalAmount,
                    itemCount,
                    method
            ));
        };

        updateSummary.run();

        dialogBinding.chipGroupPaymentMethod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateSummary.run();
            int checkedId = dialogBinding.chipGroupPaymentMethod.getCheckedChipId();
            if (checkedId == R.id.chipPayCash) {
                dialogBinding.tvQrBoxLabel.setText("Thanh Toán Tiền Mặt Tại Bàn");
            } else if (checkedId == R.id.chipPayWallet) {
                dialogBinding.tvQrBoxLabel.setText("Quét Mã Ví Điện Tử MoMo / ZaloPay");
            } else {
                dialogBinding.tvQrBoxLabel.setText("Mã QR VietQR Ngân Hàng");
            }
        });

        dialogBinding.etTableNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.tilTableNumber.setError(null);
                updateSummary.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        // Đơn được tạo (PENDING) ngay khi khách yêu cầu mã QR
        final Order[] createdOrderHolder = new Order[1];

        java.util.concurrent.Callable<Order> createPendingOrderAndShowQr = () -> {
            if (createdOrderHolder[0] != null) {
                return createdOrderHolder[0];
            }
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "";
            if (tableRaw.isEmpty()) {
                dialogBinding.tilTableNumber.setError("Vui lòng nhập số bàn trước khi thanh toán");
                return null;
            }
            String tableLabel = "Bàn " + tableRaw;
            Customer customer = authService.getCurrentCustomer();
            Integer customerId = customer != null ? customer.getId() : null;
            String paymentMethod = getSelectedPaymentMethod.call();

            Order order = orderService.createOrder(tableLabel, customerId, cartService.getCartItems(), paymentMethod);
            createdOrderHolder[0] = order;

            dialogBinding.tilTableNumber.setEnabled(false);
            String qrUrl = order.getQrPayloadUrl() != null ? order.getQrPayloadUrl() : "";
            dialogBinding.imgQrCode.setImageBitmap(QrBitmapGenerator.generateQrBitmap(qrUrl));
            dialogBinding.tvQrUrl.setText("Link VietQR: " + qrUrl);
            dialogBinding.tvQrUrl.setVisibility(View.VISIBLE);
            dialogBinding.btnOpenQrUrl.setVisibility(View.VISIBLE);
            dialogBinding.btnCreateOrderQr.setText("Đã tạo đơn " + order.getOrderId() + " ✓");
            dialogBinding.btnCreateOrderQr.setEnabled(false);
            return order;
        };

        dialogBinding.btnCreateOrderQr.setOnClickListener(v -> {
            try {
                createPendingOrderAndShowQr.call();
            } catch (Exception ignored) {}
        });

        dialogBinding.btnOpenQrUrl.setOnClickListener(v -> {
            Order current = createdOrderHolder[0];
            String url = current != null ? current.getQrPayloadUrl() : null;
            if (url == null || url.trim().isEmpty()) return;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        MainActivity.this,
                        "Thiết bị không có trình duyệt để mở mã QR. Bạn có thể nhấn giữ link ở trên để sao chép.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        dialogBinding.btnConfirmPayment.setOnClickListener(v -> {
            Order order = createdOrderHolder[0];
            if (order == null) {
                try {
                    order = createPendingOrderAndShowQr.call();
                } catch (Exception ignored) {}
            }
            if (order == null) {
                return;
            }

            cartService.clearCart();
            updateCartBadge();
            refreshAiRecommendations();
            dialog.dismiss();

            Toast.makeText(
                    MainActivity.this,
                    "Đặt hàng thành công đơn " + order.getOrderId() + " (" + order.getTableNumber() + " - " + order.getPaymentMethod() + ")! Bếp đang chế biến món ăn.",
                    Toast.LENGTH_LONG
            ).show();
        });

        dialog.show();
    }

    private void showAiChatDialog() {
        DialogAiChatBinding dialogBinding = DialogAiChatBinding.inflate(LayoutInflater.from(this));

        ChatAdapter chatAdapter = new ChatAdapter(
                aiChatService.getChatHistory(),
                item -> {
                    cartService.addItem(item, 1);
                    updateCartBadge();
                    refreshAiRecommendations();
                    Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
        );
        dialogBinding.rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.rvChatMessages.setAdapter(chatAdapter);

        Runnable refreshAndScrollChat = () -> {
            chatAdapter.updateList(aiChatService.getChatHistory());
            if (!aiChatService.getChatHistory().isEmpty()) {
                dialogBinding.rvChatMessages.scrollToPosition(aiChatService.getChatHistory().size() - 1);
            }
        };
        refreshAndScrollChat.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCloseChat.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.tvClearChatHistory.setOnClickListener(v -> {
            aiChatService.clearChat();
            refreshAndScrollChat.run();
            Toast.makeText(MainActivity.this, "Đã làm mới cuộc trò chuyện AI!", Toast.LENGTH_SHORT).show();
        });

        dialogBinding.chipQuickSpicyBudget.setOnClickListener(v -> {
            aiChatService.sendMessage("Tìm món cay ngon dưới 100k");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickBestseller.setOnClickListener(v -> {
            aiChatService.sendMessage("Gợi ý món đặc sản bán chạy nhất của quán");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickCombo.setOnClickListener(v -> {
            aiChatService.sendMessage("Đi 4 người nên gọi combo món gì");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickCrispy.setOnClickListener(v -> {
            aiChatService.sendMessage("Mình muốn ăn món nướng và món giòn rụm");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickDrinks.setOnClickListener(v -> {
            aiChatService.sendMessage("Cho xem danh mục nước uống giải khát");
            refreshAndScrollChat.run();
        });

        dialogBinding.btnChatCamera.setOnClickListener(v -> {
            String[] sampleImages = getSampleFoodImages();
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("📷 Chọn Ảnh Món Ăn Cần AI Nhận Diện")
                    .setItems(sampleImages, (imgDialog, which) -> {
                        aiChatService.analyzeFoodImage(sampleImages[which]);
                        refreshAndScrollChat.run();
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });

        Runnable sendCurrentInput = () -> {
            String userText = dialogBinding.etChatMessage.getText() != null
                    ? dialogBinding.etChatMessage.getText().toString().trim()
                    : "";
            aiChatService.sendMessage(userText);
            if (dialogBinding.etChatMessage.getText() != null) {
                dialogBinding.etChatMessage.getText().clear();
            }
            refreshAndScrollChat.run();
        };

        dialogBinding.btnSendChat.setOnClickListener(v -> sendCurrentInput.run());
        dialogBinding.etChatMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendCurrentInput.run();
            return true;
        });

        dialog.show();
    }

    private String[] getSampleFoodImages() {
        return new String[]{
                "Chân Giò Muối Chiên Giòn (Món chiên đặc sản)",
                "Ếch Nướng Muối Ớt Lá Chanh & Gà Nướng Mật Ong (Món nướng)",
                "Tôm Chiên Xù & Tôm Nướng Muối Ớt (Hải sản tươi)",
                "Chân Gà Sốt Thái & Gỏi Ngó Sen Tôm Thịt (Món phụ chua cay)",
                "Lẩu Gà Lá Giang (Món lẩu đặc biệt)",
                "Nước Ngọt Giải Khát (Coca Cola, 7Up, Pepsi, Sting)",
                "Thực đơn món nướng và món chiên giòn"
        };
    }

    private void showImageRecognitionDialog() {
        String[] sampleImages = getSampleFoodImages();

        new AlertDialog.Builder(this)
                .setTitle("📷 Chọn Ảnh Món Ăn Cần Nhận Diện")
                .setItems(sampleImages, (dialog, which) -> {
                    String selectedHint = sampleImages[which];
                    aiChatService.analyzeFoodImage(selectedHint);
                    showAiChatDialog();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showLoginDialog() {
        DialogLoginBinding dialogBinding = DialogLoginBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnLoginSubmit.setOnClickListener(v -> {
            String phoneOrEmail = dialogBinding.etPhone.getText() != null
                    ? dialogBinding.etPhone.getText().toString().trim()
                    : "";
            String password = dialogBinding.etPassword.getText() != null
                    ? dialogBinding.etPassword.getText().toString().trim()
                    : "";

            AuthResult<Customer> result = authService.loginCustomer(phoneOrEmail, password);
            if (result.isSuccess()) {
                updateUserHeader();
                refreshAiRecommendations();
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Đăng nhập thành công! Chào " + result.getData().getFullName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialogBinding.btnRegisterSubmit.setOnClickListener(v -> {
            String fullName = dialogBinding.etFullName.getText() != null
                    ? dialogBinding.etFullName.getText().toString().trim()
                    : "";
            String phone = dialogBinding.etPhone.getText() != null
                    ? dialogBinding.etPhone.getText().toString().trim()
                    : "";
            String email = dialogBinding.etEmail.getText() != null
                    ? dialogBinding.etEmail.getText().toString().trim()
                    : "";
            String password = dialogBinding.etPassword.getText() != null
                    ? dialogBinding.etPassword.getText().toString().trim()
                    : "";

            if (fullName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập họ và tên khi đăng ký!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() < 9) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập số điện thoại hợp lệ (tối thiểu 9 chữ số)!", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthResult<Customer> result = authService.register(fullName, email, phone, password);
            if (result.isSuccess()) {
                updateUserHeader();
                refreshAiRecommendations();
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Đăng ký thành công! Chào " + result.getData().getFullName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialogBinding.btnSwitchToAdmin.setOnClickListener(v -> {
            String username = dialogBinding.etAdminUsername.getText() != null
                    ? dialogBinding.etAdminUsername.getText().toString().trim()
                    : "";
            String password = dialogBinding.etAdminPassword.getText() != null
                    ? dialogBinding.etAdminPassword.getText().toString()
                    : "";

            AuthResult<AdminAccount> result = authService.loginAdmin(username, password);
            if (result.isSuccess()) {
                dialog.dismiss();
                Toast.makeText(
                        MainActivity.this,
                        "Đăng nhập quản trị thành công! Xin chào " + result.getData().getFullName(),
                        Toast.LENGTH_SHORT
                ).show();
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            } else {
                Toast.makeText(MainActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
