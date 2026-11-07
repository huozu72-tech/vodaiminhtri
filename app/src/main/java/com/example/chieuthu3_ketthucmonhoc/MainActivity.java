package com.example.chieuthu3_ketthucmonhoc;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
        String query = binding.etSearchFood.getText() != null
                ? binding.etSearchFood.getText().toString().trim()
                : "";

        List<MenuItem> items;
        if (!query.isEmpty()) {
            List<MenuItem> searchResults = menuService.searchMenuItems(query);
            items = new ArrayList<>();
            for (MenuItem item : searchResults) {
                if (item.isAvailable()) {
                    items.add(item);
                }
            }
        } else {
            int checkedId = binding.chipGroupCategory.getCheckedChipId();
            FoodCategory currentCategory = null;
            if (checkedId == R.id.chipMain) {
                currentCategory = FoodCategory.MAIN_DISH;
            } else if (checkedId == R.id.chipSide) {
                currentCategory = FoodCategory.SIDE_DISH;
            } else if (checkedId == R.id.chipDrink) {
                currentCategory = FoodCategory.BEVERAGE;
            }

            if (currentCategory != null) {
                items = new ArrayList<>(menuService.getMenuItemsByCategory(currentCategory));
            } else {
                items = new ArrayList<>(menuService.getAvailableMenuItems());
            }
        }

        // Áp dụng tiêu chí sắp xếp (Mặc định, Bán chạy nhất, Đánh giá cao, Giá thấp->cao, Giá cao->thấp)
        int sortId = binding.chipGroupSort.getCheckedChipId();
        if (sortId == R.id.chipSortPopular) {
            items.sort((a, b) -> Integer.compare(b.getOrderCount(), a.getOrderCount()));
        } else if (sortId == R.id.chipSortRating) {
            items.sort((a, b) -> {
                int cmp = Double.compare(b.getRating(), a.getRating());
                return cmp != 0 ? cmp : Integer.compare(b.getOrderCount(), a.getOrderCount());
            });
        } else if (sortId == R.id.chipSortPriceAsc) {
            items.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
        } else if (sortId == R.id.chipSortPriceDesc) {
            items.sort((a, b) -> Long.compare(b.getPrice(), a.getPrice()));
        }

        foodAdapter.updateList(items);
    }

    private void refreshAiRecommendations() {
        Customer currentCustomer = authService.getCurrentCustomer();
        Integer customerId = currentCustomer != null ? currentCustomer.getId() : null;
        List<CartItem> cartItems = cartService.getCartItems();
        if (!cartItems.isEmpty() && customerId == null) {
            List<MenuItem> inCartDishes = new ArrayList<>();
            for (CartItem ci : cartItems) {
                inCartDishes.add(ci.getMenuItem());
            }
            aiRecAdapter.updateList(aiRecService.getCartComplementaryRecommendations(inCartDishes, 5));
        } else {
            aiRecAdapter.updateList(aiRecService.getPersonalizedRecommendations(customerId, 5));
        }
    }

    private void setupSearchAndFilters() {
        binding.etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshFoodList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            if (binding.etSearchFood.getText() != null && binding.etSearchFood.getText().length() > 0) {
                binding.etSearchFood.getText().clear();
            }
            refreshFoodList();
        });

        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            refreshFoodList();
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
                    String stepProgress;
                    switch (order.getStatus()) {
                        case PENDING:
                            stepProgress = "⏳ Tiến độ: [● Chờ xác nhận] ──▶ [○ Bếp nấu] ──▶ [○ Hoàn thành]";
                            break;
                        case PREPARING:
                            stepProgress = "🔥 Tiến độ: [✓ Đã nhận] ──▶ [● Bếp đang nấu] ──▶ [○ Hoàn thành]";
                            break;
                        case COMPLETED:
                            stepProgress = "✅ Tiến độ: [✓ Đã nhận] ──▶ [✓ Đã nấu] ──▶ [● Đã phục vụ]";
                            break;
                        case CANCELLED:
                        default:
                            stepProgress = "❌ Tiến độ: Đơn hàng đã hủy";
                            break;
                    }

                    tvOrderDetails.setText(
                            stepProgress
                                    + "\nThời gian: " + order.getFormattedTime() + " • " + order.getPaymentMethod()
                                    + "\nMón: " + order.getItemsSummary()
                                    + "\nTổng thanh toán: " + order.getFormattedTotal()
                    );
                    tvOrderDetails.setTextSize(12f);
                    tvOrderDetails.setPadding(0, 6, 0, 6);
                    tvOrderDetails.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));

                    card.addView(tvOrderHeader);
                    card.addView(tvOrderDetails);

                    LinearLayout actionRow = new LinearLayout(MainActivity.this);
                    actionRow.setOrientation(LinearLayout.HORIZONTAL);

                    // Nút Đặt Lại Đơn Này (Re-order 1 chạm)
                    MaterialButton btnReorder = new MaterialButton(MainActivity.this);
                    btnReorder.setText("🔄 Đặt Lại Đơn Này");
                    btnReorder.setAllCaps(false);
                    btnReorder.setTextSize(11f);
                    btnReorder.setOnClickListener(v -> {
                        int addedCount = 0;
                        for (OrderItem oi : order.getItems()) {
                            MenuItem latestDish = menuService.getMenuItemById(oi.getMenuItem().getId());
                            if (latestDish != null && latestDish.isAvailable()) {
                                cartService.addItem(latestDish, oi.getQuantity());
                                addedCount += oi.getQuantity();
                            }
                        }
                        updateCartBadge();
                        refreshAiRecommendations();
                        dialog.dismiss();
                        Toast.makeText(
                                MainActivity.this,
                                "Đã thêm lại " + addedCount + " phần từ đơn " + order.getOrderId() + " vào giỏ hàng!",
                                Toast.LENGTH_SHORT
                        ).show();
                        showCartBottomSheet();
                    });
                    actionRow.addView(btnReorder);

                    // Nút hủy đơn nếu đơn đang Chờ xác nhận (PENDING) - UC-06 Theo dõi đơn hàng
                    if (order.getStatus() == OrderStatus.PENDING) {
                        MaterialButton btnCancelOrder = new MaterialButton(MainActivity.this);
                        btnCancelOrder.setText("❌ Hủy Đơn");
                        btnCancelOrder.setAllCaps(false);
                        btnCancelOrder.setTextSize(11f);
                        btnCancelOrder.setBackgroundColor(Color.parseColor("#FEE2E2"));
                        btnCancelOrder.setTextColor(Color.parseColor("#DC2626"));
                        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cancelParams.setMargins(12, 0, 0, 0);
                        btnCancelOrder.setLayoutParams(cancelParams);
                        btnCancelOrder.setOnClickListener(v -> {
                            boolean cancelled = orderService.cancelOrder(order.getOrderId());
                            if (cancelled) {
                                refreshFoodList();
                                Toast.makeText(
                                        MainActivity.this,
                                        "Đã hủy đơn hàng " + order.getOrderId() + " thành công!",
                                        Toast.LENGTH_SHORT
                                ).show();
                                this.run();
                            }
                        });
                        actionRow.addView(btnCancelOrder);
                    }

                    card.addView(actionRow);
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

        Runnable refreshCartSheetState = new Runnable() {
            @Override
            public void run() {
                List<CartItem> items = cartService.getCartItems();
                cartAdapterHolder[0].updateList(items);
                updateCartBadge();
                refreshAiRecommendations();
                calculateCartTotal(dialogBinding);

                if (items.isEmpty()) {
                    dialogBinding.tvEmptyCartHint.setVisibility(View.VISIBLE);
                    dialogBinding.rvCartItems.setVisibility(View.GONE);
                    dialogBinding.layoutCartAiUpsellSection.setVisibility(View.GONE);
                } else {
                    dialogBinding.tvEmptyCartHint.setVisibility(View.GONE);
                    dialogBinding.rvCartItems.setVisibility(View.VISIBLE);

                    List<MenuItem> currentDishes = new ArrayList<>();
                    for (CartItem ci : items) {
                        currentDishes.add(ci.getMenuItem());
                    }
                    List<AIRecommendation> upsells = aiRecService.getCartComplementaryRecommendations(currentDishes, 2);
                    dialogBinding.layoutCartAiUpsellItems.removeAllViews();
                    if (upsells.isEmpty()) {
                        dialogBinding.layoutCartAiUpsellSection.setVisibility(View.GONE);
                    } else {
                        dialogBinding.layoutCartAiUpsellSection.setVisibility(View.VISIBLE);
                        for (AIRecommendation rec : upsells) {
                            MenuItem upDish = rec.getMenuItem();
                            LinearLayout row = new LinearLayout(MainActivity.this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setGravity(Gravity.CENTER_VERTICAL);
                            row.setPadding(8, 6, 8, 6);

                            TextView tvUp = new TextView(MainActivity.this);
                            tvUp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            tvUp.setText("• " + upDish.getName() + " (" + upDish.getFormattedPrice() + ") — " + rec.getReason());
                            tvUp.setTextSize(11f);
                            tvUp.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));

                            MaterialButton btnAddUp = new MaterialButton(MainActivity.this);
                            btnAddUp.setText("+ Thêm");
                            btnAddUp.setAllCaps(false);
                            btnAddUp.setTextSize(10f);
                            btnAddUp.setOnClickListener(v -> {
                                cartService.addItem(upDish, 1);
                                this.run();
                                Toast.makeText(MainActivity.this, "Đã ghép thêm '" + upDish.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
                            });

                            row.addView(tvUp);
                            row.addView(btnAddUp);
                            dialogBinding.layoutCartAiUpsellItems.addView(row);
                        }
                    }
                }
            }
        };

        cartAdapterHolder[0] = new CartAdapter(
                cartService.getCartItems(),
                new CartAdapter.OnCartItemActionListener() {
                    @Override
                    public void onQuantityPlus(CartItem item) {
                        cartService.updateQuantity(item.getMenuItem().getId(), item.getQuantity() + 1);
                        refreshCartSheetState.run();
                    }

                    @Override
                    public void onQuantityMinus(CartItem item) {
                        cartService.updateQuantity(item.getMenuItem().getId(), item.getQuantity() - 1);
                        refreshCartSheetState.run();
                    }

                    @Override
                    public void onRemoveItem(CartItem item) {
                        cartService.removeItem(item.getMenuItem().getId());
                        refreshCartSheetState.run();
                        Toast.makeText(MainActivity.this, "Đã xóa '" + item.getMenuItem().getName() + "' khỏi giỏ!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onEditNote(CartItem item) {
                        String[] presetNotes = new String[]{
                                "Ít cay",
                                "Không hành / ngò",
                                "Chiên giòn kỹ",
                                "Ít đá / Không đá",
                                "Nêm đậm đà",
                                "Mang ra trước",
                                "Xóa ghi chú"
                        };
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("📝 Ghi Chú Bếp: " + item.getMenuItem().getName())
                                .setItems(presetNotes, (d, which) -> {
                                    String chosen = which == 6 ? "" : presetNotes[which];
                                    cartService.updateNote(item.getMenuItem().getId(), chosen);
                                    refreshCartSheetState.run();
                                })
                                .setNegativeButton("Đóng", null)
                                .show();
                    }
                }
        );

        dialogBinding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.rvCartItems.setAdapter(cartAdapterHolder[0]);

        refreshCartSheetState.run();

        dialogBinding.tvClearCart.setOnClickListener(v -> {
            cartService.clearCart();
            refreshCartSheetState.run();
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

        List<CartItem> currentCartItems = cartService.getCartItems();
        long totalAmount = cartService.getTotalAmount();
        int itemCount = cartService.getTotalItemCount();
        DialogPaymentQrBinding dialogBinding = DialogPaymentQrBinding.inflate(LayoutInflater.from(this));

        // 1. Hiển thị bảng kê chi tiết từng món trong hóa đơn
        dialogBinding.layoutPaymentItemsBreakdown.removeAllViews();
        for (int i = 0; i < currentCartItems.size(); i++) {
            CartItem ci = currentCartItems.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 4, 0, 4);

            TextView tvLeft = new TextView(this);
            tvLeft.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvLeft.setText((i + 1) + ". " + ci.getMenuItem().getName() + " (x" + ci.getQuantity() + ")");
            tvLeft.setTextSize(12f);
            tvLeft.setTextColor(ContextCompat.getColor(this, R.color.black));

            TextView tvRight = new TextView(this);
            tvRight.setText(String.format(Locale.getDefault(), "%,d đ", ci.getSubtotal()));
            tvRight.setTextSize(12f);
            tvRight.setTypeface(null, android.graphics.Typeface.BOLD);
            tvRight.setTextColor(ContextCompat.getColor(this, R.color.black));

            row.addView(tvLeft);
            row.addView(tvRight);
            dialogBinding.layoutPaymentItemsBreakdown.addView(row);
        }
        dialogBinding.tvPaymentTotalAmount.setText(String.format(Locale.getDefault(), "%,d đ", totalAmount));

        // 2. Tự động điền số bàn từ hồ sơ khách hàng (nếu có) hoặc mặc định Bàn 12
        Customer currentCustomer = authService.getCurrentCustomer();
        String defaultTable = "12";
        if (currentCustomer != null && currentCustomer.getAddress() != null) {
            String digits = currentCustomer.getAddress().replaceAll("[^0-9]", "");
            if (!digits.isEmpty() && digits.length() <= 3) {
                defaultTable = digits;
            }
        }
        dialogBinding.etTableNumber.setText(defaultTable);
        dialogBinding.etCashGiven.setText(String.valueOf(totalAmount));

        java.util.concurrent.Callable<String> getSelectedPaymentMethod = () -> {
            int checkedId = dialogBinding.chipGroupPaymentMethod.getCheckedChipId();
            if (checkedId == R.id.chipPayCash) {
                return "Tiền mặt tại bàn";
            } else if (checkedId == R.id.chipPayWallet) {
                return "Ví MoMo / ZaloPay";
            }
            return "Chuyển khoản QR";
        };

        // Hàm cập nhật giao diện động theo Số bàn & Phương thức thanh toán
        Runnable updatePaymentUiState = () -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "";
            String cleanTable = !tableRaw.isEmpty() ? tableRaw : "12";
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

            String transferNote = "TT BAN " + cleanTable + " " + (totalAmount / 1000) + "K";
            dialogBinding.tvTransferContent.setText("📝 Nội dung: " + transferNote);

            int checkedId = dialogBinding.chipGroupPaymentMethod.getCheckedChipId();
            if (checkedId == R.id.chipPayCash) {
                // Chế độ Tiền mặt tại bàn: Ẩn mã QR, hiện bảng tính tiền mặt & tiền thừa
                dialogBinding.layoutQrPaymentSection.setVisibility(View.GONE);
                dialogBinding.layoutCashPaymentSection.setVisibility(View.VISIBLE);
                dialogBinding.btnConfirmPayment.setText("✅ Xác Nhận Đặt Hàng (Thu Tiền Mặt Tại Bàn)");
            } else if (checkedId == R.id.chipPayWallet) {
                // Chế độ Ví điện tử MoMo / ZaloPay
                dialogBinding.layoutQrPaymentSection.setVisibility(View.VISIBLE);
                dialogBinding.layoutCashPaymentSection.setVisibility(View.GONE);
                dialogBinding.tvQrBoxLabel.setText("Mã QR Ví Điện Tử MoMo / ZaloPay (Chuẩn EMVCo)");
                dialogBinding.tvBankProviderName.setText("📱 Ví điện tử: MoMo / ZaloPay Business");
                dialogBinding.tvBankAccountOwner.setText("👤 Chủ ví nhận: NHA HANG CHIEU THU 3");
                dialogBinding.tvBankAccountNumber.setText("💳 SĐT Ví: 0901234567");
                dialogBinding.btnConfirmPayment.setText("✅ Xác Nhận Đã Thanh Toán Ví & Đặt Món");

                String emvPayload = QrBitmapGenerator.buildVietQrEmvPayload("970422", "0901234567", totalAmount, transferNote);
                dialogBinding.imgQrCode.setImageBitmap(QrBitmapGenerator.generateQrBitmap(emvPayload));
            } else {
                // Chế độ Chuyển khoản Ngân hàng VietQR Napas247
                dialogBinding.layoutQrPaymentSection.setVisibility(View.VISIBLE);
                dialogBinding.layoutCashPaymentSection.setVisibility(View.GONE);
                dialogBinding.tvQrBoxLabel.setText("Mã VietQR Napas247 (Quét Bằng App Ngân Hàng)");
                dialogBinding.tvBankProviderName.setText("🏦 Ngân hàng: MB Bank (Ngân hàng Quân Đội - BIN 970422)");
                dialogBinding.tvBankAccountOwner.setText("👤 Chủ tài khoản: NHA HANG CHIEU THU 3");
                dialogBinding.tvBankAccountNumber.setText("💳 Số TK: 0901234567");
                dialogBinding.btnConfirmPayment.setText("✅ Xác Nhận Đã Chuyển Khoản & Đặt Món");

                String emvPayload = QrBitmapGenerator.buildVietQrEmvPayload("970422", "0901234567", totalAmount, transferNote);
                dialogBinding.imgQrCode.setImageBitmap(QrBitmapGenerator.generateQrBitmap(emvPayload));
            }
        };

        // Hàm tính tiền thừa trả khách theo thời gian thực khi chọn Tiền mặt
        Runnable updateCashChange = () -> {
            String rawCash = dialogBinding.etCashGiven.getText() != null
                    ? dialogBinding.etCashGiven.getText().toString().trim()
                    : "";
            long cashGiven = 0L;
            try {
                cashGiven = Long.parseLong(rawCash);
            } catch (NumberFormatException ignored) {}

            if (cashGiven >= totalAmount) {
                long change = cashGiven - totalAmount;
                dialogBinding.tvCashChangeAmount.setText(
                        String.format(Locale.getDefault(), "💰 Tiền thừa trả lại khách: %,d đ", change)
                );
                dialogBinding.tvCashChangeAmount.setTextColor(Color.parseColor("#15803D"));
            } else {
                long missing = totalAmount - cashGiven;
                dialogBinding.tvCashChangeAmount.setText(
                        String.format(Locale.getDefault(), "⚠️ Khách đưa còn thiếu: %,d đ", missing)
                );
                dialogBinding.tvCashChangeAmount.setTextColor(Color.parseColor("#DC2626"));
            }
        };

        updatePaymentUiState.run();
        updateCashChange.run();

        dialogBinding.chipGroupPaymentMethod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updatePaymentUiState.run();
        });

        dialogBinding.etTableNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.tilTableNumber.setError(null);
                updatePaymentUiState.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialogBinding.etCashGiven.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCashChange.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Các nút chọn mệnh giá tiền mặt nhanh
        dialogBinding.btnCashExact.setOnClickListener(v -> dialogBinding.etCashGiven.setText(String.valueOf(totalAmount)));
        dialogBinding.btnCash200k.setOnClickListener(v -> dialogBinding.etCashGiven.setText("200000"));
        dialogBinding.btnCash500k.setOnClickListener(v -> dialogBinding.etCashGiven.setText("500000"));
        dialogBinding.btnCash1000k.setOnClickListener(v -> dialogBinding.etCashGiven.setText("1000000"));

        // Các nút sao chép Số tài khoản và Nội dung chuyển khoản vào bộ nhớ tạm (Clipboard)
        dialogBinding.btnCopyAccountNo.setOnClickListener(v -> {
            copyToClipboard("Số tài khoản nhà hàng", "0901234567");
        });

        dialogBinding.btnCopyTransferContent.setOnClickListener(v -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "12";
            String transferNote = "TT BAN " + (tableRaw.isEmpty() ? "12" : tableRaw) + " " + (totalAmount / 1000) + "K";
            copyToClipboard("Nội dung chuyển khoản", transferNote);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnOpenQrUrl.setOnClickListener(v -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "12";
            String url = "https://img.vietqr.io/image/MB-0901234567-compact2.png?amount=" + totalAmount
                    + "&addInfo=TT%20BAN%20" + (tableRaw.isEmpty() ? "12" : tableRaw)
                    + "&accountName=NHA%20HANG%20CHIEU%20THU%203";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        MainActivity.this,
                        "Thiết bị không có trình duyệt để mở trang VietQR.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        dialogBinding.btnConfirmPayment.setOnClickListener(v -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "";
            if (tableRaw.isEmpty()) {
                dialogBinding.tilTableNumber.setError("Vui lòng nhập số bàn phục vụ trước khi xác nhận!");
                return;
            }

            String paymentMethod = "Chuyển khoản QR";
            try {
                paymentMethod = getSelectedPaymentMethod.call();
            } catch (Exception ignored) {}

            long cashGiven = totalAmount;
            long cashChange = 0L;
            if (dialogBinding.chipGroupPaymentMethod.getCheckedChipId() == R.id.chipPayCash) {
                String rawCash = dialogBinding.etCashGiven.getText() != null
                        ? dialogBinding.etCashGiven.getText().toString().trim()
                        : "";
                try {
                    cashGiven = Long.parseLong(rawCash);
                } catch (NumberFormatException e) {
                    cashGiven = 0L;
                }
                if (cashGiven < totalAmount) {
                    Toast.makeText(
                            MainActivity.this,
                            "Số tiền khách đưa chưa đủ để thanh toán hóa đơn (" + String.format(Locale.getDefault(), "%,d đ", totalAmount) + ")!",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                cashChange = cashGiven - totalAmount;
            }

            String tableLabel = "Bàn " + tableRaw;
            Customer customer = authService.getCurrentCustomer();
            Integer customerId = customer != null ? customer.getId() : null;

            Order createdOrder = orderService.createOrder(tableLabel, customerId, cartService.getCartItems(), paymentMethod);

            cartService.clearCart();
            updateCartBadge();
            refreshFoodList();
            refreshAiRecommendations();
            dialog.dismiss();

            showPaymentSuccessReceiptDialog(createdOrder, cashGiven, cashChange);
        });

        dialog.show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "✅ Đã sao chép " + label + ": " + text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị Biên Lai Giao Dịch Thanh Toán Điện Tử sau khi khách xác nhận đặt hàng
     */
    private void showPaymentSuccessReceiptDialog(Order order, long cashGiven, long cashChange) {
        String txPrefix;
        if (order.getPaymentMethod().contains("Tiền mặt")) {
            txPrefix = "CASH-";
        } else if (order.getPaymentMethod().contains("MoMo") || order.getPaymentMethod().contains("Ví")) {
            txPrefix = "MOMO-";
        } else {
            txPrefix = "FT26";
        }
        String transactionCode = txPrefix + (System.currentTimeMillis() % 100000000L);

        StringBuilder receipt = new StringBuilder();
        receipt.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        receipt.append("🧾 BIÊN LAI XÁC NHẬN THANH TOÁN\n");
        receipt.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        receipt.append("• Mã giao dịch: ").append(transactionCode).append("\n");
        receipt.append("• Mã đơn hàng: ").append(order.getOrderId()).append("\n");
        receipt.append("• Bàn phục vụ: ").append(order.getTableNumber()).append("\n");
        receipt.append("• Thời gian: ").append(order.getFormattedTime()).append("\n");
        receipt.append("• Phương thức: ").append(order.getPaymentMethod()).append("\n");
        receipt.append("• Trạng thái đơn: ").append(order.getStatus().getDisplayName()).append(" (Bếp đã nhận lệnh)\n\n");
        receipt.append("🍽️ Chi tiết món đã đặt:\n");
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            receipt.append("  ").append(i + 1).append(". ")
                    .append(item.getMenuItem().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(String.format(Locale.getDefault(), "%,d đ", item.getSubtotal()))
                    .append("\n");
        }
        receipt.append("\n💰 TỔNG THANH TOÁN: ").append(order.getFormattedTotal()).append("\n");
        if (order.getPaymentMethod().contains("Tiền mặt")) {
            receipt.append("💵 Tiền khách đưa: ").append(String.format(Locale.getDefault(), "%,d đ", cashGiven)).append("\n");
            receipt.append("🪙 Tiền thừa trả khách: ").append(String.format(Locale.getDefault(), "%,d đ", cashChange)).append("\n");
        } else {
            receipt.append("🏦 Tài khoản thụ hưởng: MBBank - 0901234567\n");
            receipt.append("✔️ Xác thực giao dịch: Đã ghi nhận thành công\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("✅ Thanh Toán & Đặt Món Thành Công!")
                .setMessage(receipt.toString().trim())
                .setPositiveButton("📜 Theo Dõi Đơn Hàng", (d, w) -> {
                    Customer c = authService.getCurrentCustomer();
                    if (c != null) {
                        showCustomerProfileDialog(c);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
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
