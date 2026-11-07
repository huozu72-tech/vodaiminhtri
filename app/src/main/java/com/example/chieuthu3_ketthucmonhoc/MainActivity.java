package com.example.chieuthu3_ketthucmonhoc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogLoginBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogPaymentQrBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIChatbotService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIRecommendationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.ICartService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.QrBitmapGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
        foodAdapter = new FoodAdapter(menuService.getAvailableMenuItems(), item -> {
            cartService.addItem(item, 1);
            updateCartBadge();
            refreshAiRecommendations();
            Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
        });
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
                }
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

    private void showCustomerProfileDialog(Customer customer) {
        DialogCustomerProfileBinding dialogBinding = DialogCustomerProfileBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.tvProfileFullName.setText(customer.getFullName());
        dialogBinding.tvProfilePhone.setText("Số điện thoại: " + customer.getPhoneNumber());
        dialogBinding.tvProfileEmail.setText(
                customer.getEmail() != null && !customer.getEmail().trim().isEmpty()
                        ? "Email: " + customer.getEmail()
                        : "Email: Chưa cập nhật"
        );

        List<Order> orders = orderService.getCustomerOrders(customer.getId());
        if (orders.isEmpty()) {
            dialogBinding.tvNoOrdersHint.setVisibility(View.VISIBLE);
        } else {
            dialogBinding.tvNoOrdersHint.setVisibility(View.GONE);
            int displayCount = Math.min(orders.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                Order order = orders.get(i);
                TextView orderItemView = new TextView(this);
                String statusText;
                switch (order.getStatus()) {
                    case PENDING:
                        statusText = "Chờ xác nhận";
                        break;
                    case PREPARING:
                        statusText = "Bếp đang nấu";
                        break;
                    case COMPLETED:
                        statusText = "Đã hoàn thành ✓";
                        break;
                    case CANCELLED:
                    default:
                        statusText = "Đã hủy";
                        break;
                }

                StringBuilder itemsSummary = new StringBuilder();
                for (int j = 0; j < order.getItems().size(); j++) {
                    OrderItem orderItem = order.getItems().get(j);
                    if (j > 0) itemsSummary.append(", ");
                    itemsSummary.append(orderItem.getMenuItem().getName())
                            .append(" x")
                            .append(orderItem.getQuantity());
                }

                orderItemView.setText(order.getOrderId() + " • " + order.getFormattedTotal() + " • " + statusText + "\n(" + itemsSummary + ")");
                orderItemView.setTextSize(12f);
                orderItemView.setPadding(24, 16, 24, 16);
                orderItemView.setBackgroundResource(R.drawable.bg_card_food);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                orderItemView.setLayoutParams(params);

                dialogBinding.layoutCustomerOrders.addView(orderItemView);
            }
        }

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
            return
;
        }

        long totalAmount = cartService.getTotalAmount();
        int itemCount = cartService.getTotalItemCount();
        DialogPaymentQrBinding dialogBinding = DialogPaymentQrBinding.inflate(LayoutInflater.from(this));

        dialogBinding.etTableNumber.setText("12");

        Runnable updateSummary = () -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "";
            String tableLabel = !tableRaw.isEmpty() ? "Bàn " + tableRaw : "Bàn ?";
            dialogBinding.tvOrderSummary.setText(String.format(
                    Locale.getDefault(),
                    "Hóa đơn %s • Tổng cộng: %,d đ (%d món)",
                    tableLabel,
                    totalAmount,
                    itemCount
            ));
        };

        updateSummary.run();

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

            Order order = orderService.createOrder(tableLabel, customerId, cartService.getCartItems());
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
                    "Thanh toán thành công đơn " + order.getOrderId() + " (" + order.getTableNumber() + ")! Bếp đang chế biến món ăn.",
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
