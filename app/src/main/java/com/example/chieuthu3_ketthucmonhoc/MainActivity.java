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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chieuthu3_ketthucmonhoc.adapters.CartAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.ChatAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.FoodAdapter;
import com.example.chieuthu3_ketthucmonhoc.adapters.RecommendationAdapter;
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityMainBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCartBottomSheetBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogChatBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCustomerProfileBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogFoodDetailBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogPaymentQrBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.utils.FoodImageLoader;
import com.example.chieuthu3_ketthucmonhoc.domain.models.LoyaltyProfile;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.PromoVoucher;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Recommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.RestaurantTable;
import com.example.chieuthu3_ketthucmonhoc.domain.models.SmartComboProposal;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableServiceRequest;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TableStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.utils.VietnameseTelexHelper;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.ICartService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IChatbotService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IRecommendationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IRestaurantOperationsService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.QrBitmapGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FoodAdapter foodAdapter;
    private RecommendationAdapter recAdapter;

    // Services via AppServiceLocator (Clean OOP Architecture)
    private IMenuService menuService;
    private ICartService cartService;
    private IOrderService orderService;
    private IAuthenticationService authService;
    private IRecommendationService recService;
    private IChatbotService chatService;
    private IRestaurantOperationsService opsService;

    private Runnable activeChatRefreshRunnable;
    private File pendingCameraPhotoFile;

    private final ActivityResultLauncher<String> pickSearchImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                String displayName = FoodImageLoader.getDisplayName(this, uri);
                String savedPath = FoodImageLoader.saveScanImageToInternalStorage(this, uri);
                if (savedPath == null || savedPath.isEmpty()) {
                    Toast.makeText(this, "Không thể đọc ảnh đã chọn. Vui lòng thử ảnh khác!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String visualHint = FoodImageLoader.buildFoodRecognitionHint(this, savedPath, displayName);
                handleRecognizedFoodPhoto(displayName, visualHint, savedPath);
            });

    private final ActivityResultLauncher<Uri> takeFoodPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                File capturedFile = pendingCameraPhotoFile;
                pendingCameraPhotoFile = null;
                if (!Boolean.TRUE.equals(success) || capturedFile == null || !capturedFile.exists()) {
                    return;
                }
                Uri fileUri = Uri.fromFile(capturedFile);
                String savedPath = FoodImageLoader.saveScanImageToInternalStorage(this, fileUri);
                String targetPath = (savedPath != null && !savedPath.isEmpty())
                        ? savedPath
                        : capturedFile.getAbsolutePath();
                String visualHint = FoodImageLoader.buildFoodRecognitionHint(this, targetPath, capturedFile.getName());
                handleRecognizedFoodPhoto("Ảnh chụp Camera", visualHint, targetPath);
            });

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
        recService = AppServiceLocator.getRecommendationService();
        chatService = AppServiceLocator.getChatbotService();
        opsService = AppServiceLocator.getOperationsService();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupFoodGrid();
        setupRecommendations();
        setupSearchAndFilters();
        setupListeners();
        updateCartBadge();
        updateUserHeader();
        updateTableButtonLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFoodList();
        refreshRecommendations();
        updateCartBadge();
        updateUserHeader();
        updateTableButtonLabel();
    }

    private void updateTableButtonLabel() {
        String tbl = opsService.getSelectedTableNumber();
        String label = (tbl.toLowerCase().startsWith("bàn") || tbl.toUpperCase().startsWith("VIP"))
                ? tbl
                : "Bàn " + tbl;
        binding.btnQuickTableMap.setText("🪑 Sơ Đồ Bàn (" + label + ")");
    }

    private void setupFoodGrid() {
        foodAdapter = new FoodAdapter(
                menuService.getAvailableMenuItems(),
                item -> {
                    cartService.addItem(item, 1);
                    updateCartBadge();
                    refreshRecommendations();
                    Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
                },
                this::showFoodDetailDialog
        );
        binding.rvFoodGrid.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvFoodGrid.setAdapter(foodAdapter);
    }

    private void setupRecommendations() {
        Customer currentCustomer = authService.getCurrentCustomer();
        Integer customerId = currentCustomer != null ? currentCustomer.getId() : null;
        recAdapter = new RecommendationAdapter(
                recService.getPersonalizedRecommendations(customerId, 5),
                item -> {
                    cartService.addItem(item, 1);
                    updateCartBadge();
                    Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ!", Toast.LENGTH_SHORT).show();
                },
                this::showFoodDetailDialog
        );
        binding.rvRecommendations.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvRecommendations.setAdapter(recAdapter);
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
            if (checkedId == R.id.chipFavorites) {
                items = new ArrayList<>();
                for (MenuItem fav : opsService.getFavoriteDishes()) {
                    if (fav.isAvailable()) {
                        items.add(fav);
                    }
                }
            } else {
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

    private void refreshRecommendations() {
        Customer currentCustomer = authService.getCurrentCustomer();
        Integer customerId = currentCustomer != null ? currentCustomer.getId() : null;
        List<CartItem> cartItems = cartService.getCartItems();
        if (!cartItems.isEmpty() && customerId == null) {
            List<MenuItem> inCartDishes = new ArrayList<>();
            for (CartItem ci : cartItems) {
                inCartDishes.add(ci.getMenuItem());
            }
            recAdapter.updateList(recService.getCartComplementaryRecommendations(inCartDishes, 5));
        } else {
            recAdapter.updateList(recService.getPersonalizedRecommendations(customerId, 5));
        }
    }

    private void setupSearchAndFilters() {
        VietnameseTelexHelper.attachToEditText(binding.etSearchFood, null);
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

        binding.btnQuickTableMap.setOnClickListener(v -> showTableReservationDialog());
        binding.btnQuickSmartCombo.setOnClickListener(v -> showSmartComboBuilderDialog());
        binding.btnQuickVouchers.setOnClickListener(v -> showVoucherWalletDialog(null));
        binding.btnQuickCallWaiter.setOnClickListener(v -> showCallWaiterDialog());

        binding.btnUploadFoodPhoto.setOnClickListener(v -> showImageRecognitionDialog());
        binding.fabChatbot.setOnClickListener(v -> showChatDialog());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                binding.chipAll.setChecked(true);
                foodAdapter.updateList(menuService.getAvailableMenuItems());
                return true;
            } else if (itemId == R.id.nav_chatbot) {
                showChatDialog();
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
     * Xem chi tiết món ăn (Hình 10 Activity Diagram) + Đánh giá món ăn (UC-07) + Dinh dưỡng Calo + Yêu thích + Món tương tự gợi ý
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
            FoodImageLoader.loadFoodImage(dialogBinding.imgDetailFood, dish);
            dialogBinding.tvDetailCategoryBadge.setText(dish.getCategory().getDisplayName());
            dialogBinding.tvDetailFoodName.setText(dish.getName());
            dialogBinding.tvDetailFoodPrice.setText(dish.getFormattedPrice());
            dialogBinding.tvDetailRatingSold.setText(
                    String.format(Locale.US, "⭐ %.1f / 5.0 • 🔥 Đã phục vụ %d phần", dish.getRating(), dish.getOrderCount())
            );
            dialogBinding.tvDetailFoodDesc.setText(dish.getDescription());

            int kcal = recService.estimateDishCalories(dish);
            String servingHint = dish.getCategory() == FoodCategory.MAIN_DISH
                    ? "Khẩu phần 2–3 người"
                    : (dish.getCategory() == FoodCategory.SIDE_DISH ? "Khẩu phần 1–2 người" : "Giải khát cá nhân");
            dialogBinding.tvDetailNutritionInfo.setText(
                    String.format(Locale.getDefault(), "🥗 Dinh dưỡng ước tính: ~%d kcal/phần • %s", kcal, servingHint)
            );

            boolean isFav = opsService.isFavoriteDish(dish.getId());
            dialogBinding.btnToggleFavorite.setText(isFav ? "❤️ Đã thích" : "🤍 Yêu thích");

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

        dialogBinding.btnToggleFavorite.setOnClickListener(v -> {
            MenuItem dish = dishHolder[0];
            boolean nowFav = opsService.toggleFavoriteDish(dish.getId());
            bindDishData.run();
            refreshFoodList();
            Toast.makeText(
                    MainActivity.this,
                    nowFav ? "❤️ Đã lưu '" + dish.getName() + "' vào danh sách Yêu Thích!" : "Đã bỏ '" + dish.getName() + "' khỏi Yêu Thích",
                    Toast.LENGTH_SHORT
            ).show();
        });

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
            refreshRecommendations();
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
                refreshRecommendations();
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

        // Similar Dishes (Hình 10: Món tương tự từ Hệ thống gợi ý)
        List<Recommendation> similarDishes = recService.getSimilarDishes(dishHolder[0].getId(), 3);
        dialogBinding.layoutSimilarDishesContainer.removeAllViews();
        for (Recommendation rec : similarDishes) {
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
                refreshRecommendations();
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

    /**
     * Tính năng mới 1: Sơ đồ bàn trực quan & Đặt bàn trước (Table Floor Map & Reservation)
     */
    private void showTableReservationDialog() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(36, 24, 36, 24);
        scrollView.addView(container);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🪑 Sơ Đồ Bàn & Đặt Chỗ Trước")
                .setView(scrollView)
                .setNegativeButton("Đóng", null)
                .create();

        Runnable renderTables = new Runnable() {
            @Override
            public void run() {
                container.removeAllViews();
                TextView tvHeader = new TextView(MainActivity.this);
                tvHeader.setText(
                        "📍 Bàn đang chọn của bạn: Bàn " + opsService.getSelectedTableNumber()
                                + "\nChạm vào bàn trống để đổi bàn ngồi hoặc đặt lịch hẹn trước:"
                );
                tvHeader.setTextSize(12f);
                tvHeader.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.gray_text));
                tvHeader.setPadding(0, 0, 0, 16);
                container.addView(tvHeader);

                for (RestaurantTable table : opsService.getAllTables()) {
                    LinearLayout card = new LinearLayout(MainActivity.this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(24, 18, 24, 18);
                    card.setBackgroundResource(R.drawable.bg_card_food);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    lp.setMargins(0, 8, 0, 8);
                    card.setLayoutParams(lp);

                    String statusBadge;
                    String statusColor;
                    if (table.getStatus() == TableStatus.AVAILABLE) {
                        statusBadge = "🟢 " + table.getStatus().getDisplayName();
                        statusColor = "#15803D";
                    } else if (table.getStatus() == TableStatus.RESERVED) {
                        statusBadge = "🟡 " + table.getStatus().getDisplayName() + " (" + table.getReservationTime() + ")";
                        statusColor = "#D97706";
                    } else {
                        statusBadge = "🔴 " + table.getStatus().getDisplayName();
                        statusColor = "#DC2626";
                    }

                    TextView tvTitle = new TextView(MainActivity.this);
                    tvTitle.setText("🪑 " + table.getTableNumber() + " • " + table.getZone() + " (Sức chứa: " + table.getCapacity() + " khách)");
                    tvTitle.setTextSize(13f);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvTitle.setTextColor(Color.BLACK);

                    TextView tvStatus = new TextView(MainActivity.this);
                    String detail = statusBadge;
                    if (!table.getReservedByName().isEmpty()) {
                        detail += " • Khách: " + table.getReservedByName();
                    }
                    if (!table.getNote().isEmpty()) {
                        detail += " (" + table.getNote() + ")";
                    }
                    tvStatus.setText(detail);
                    tvStatus.setTextSize(11f);
                    tvStatus.setTextColor(Color.parseColor(statusColor));
                    tvStatus.setPadding(0, 4, 0, 8);

                    LinearLayout btnRow = new LinearLayout(MainActivity.this);
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);

                    MaterialButton btnSitHere = new MaterialButton(MainActivity.this);
                    btnSitHere.setText("📍 Chọn Ngồi Bàn Này");
                    btnSitHere.setAllCaps(false);
                    btnSitHere.setTextSize(10f);
                    btnSitHere.setOnClickListener(v -> {
                        opsService.occupyTable(table.getTableNumber(), 2);
                        updateTableButtonLabel();
                        Toast.makeText(MainActivity.this, "✅ Đã chọn phục vụ tại " + table.getTableNumber(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                    MaterialButton btnReserve = new MaterialButton(MainActivity.this);
                    btnReserve.setText("📅 Đặt Trước");
                    btnReserve.setAllCaps(false);
                    btnReserve.setTextSize(10f);
                    LinearLayout.LayoutParams resParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    resParams.setMargins(12, 0, 0, 0);
                    btnReserve.setLayoutParams(resParams);
                    btnReserve.setOnClickListener(v -> {
                        showReserveTableFormDialog(table, this);
                    });

                    btnRow.addView(btnSitHere);
                    btnRow.addView(btnReserve);

                    card.addView(tvTitle);
                    card.addView(tvStatus);
                    card.addView(btnRow);
                    container.addView(card);
                }
            }
        };

        renderTables.run();
        dialog.show();
    }

    private void showReserveTableFormDialog(RestaurantTable table, Runnable onReservedCallback) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(42, 24, 42, 12);

        Customer currentCustomer = authService.getCurrentCustomer();

        EditText etName = new EditText(this);
        etName.setHint("Họ tên người đặt bàn");
        if (currentCustomer != null) etName.setText(currentCustomer.getFullName());

        EditText etPhone = new EditText(this);
        etPhone.setHint("Số điện thoại liên hệ");
        if (currentCustomer != null) etPhone.setText(currentCustomer.getPhoneNumber());

        EditText etTime = new EditText(this);
        etTime.setHint("Khung giờ nhận bàn (VD: 19:00 tối nay)");
        etTime.setText("19:00");

        EditText etGuests = new EditText(this);
        etGuests.setHint("Số lượng khách (VD: " + table.getCapacity() + ")");
        etGuests.setText(String.valueOf(table.getCapacity()));

        EditText etNote = new EditText(this);
        etNote.setHint("Ghi chú (Tiệc sinh nhật, ghế trẻ em, gần cửa sổ...)");

        form.addView(etName);
        form.addView(etPhone);
        form.addView(etTime);
        form.addView(etGuests);
        form.addView(etNote);

        new AlertDialog.Builder(this)
                .setTitle("📅 Đặt Trước " + table.getTableNumber() + " (" + table.getZone() + ")")
                .setView(form)
                .setPositiveButton("✅ Xác Nhận Đặt Bàn", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    int guests = table.getCapacity();
                    try {
                        guests = Integer.parseInt(etGuests.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}
                    String note = etNote.getText().toString().trim();

                    opsService.reserveTable(
                            table.getTableNumber(),
                            name.isEmpty() ? "Khách đặt trước" : name,
                            phone.isEmpty() ? "0901234567" : phone,
                            time.isEmpty() ? "19:00" : time,
                            guests,
                            note
                    );
                    if (onReservedCallback != null) onReservedCallback.run();
                    Toast.makeText(
                            MainActivity.this,
                            "🎉 Đã giữ chỗ " + table.getTableNumber() + " lúc " + time + " thành công!",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Tính năng mới 2: Thiết kế Mâm cơm / Combo tự động theo Số người & Ngân sách
     */
    private void showSmartComboBuilderDialog() {
        String[] presetConfigs = new String[]{
                "👫 2 Người • Ngân sách ~200.000 đ • Cân bằng Đủ vị",
                "👨‍👩‍👧‍👦 4 Người • Ngân sách ~400.000 đ • Đặc sản Bán chạy",
                "🌶️ 4 Người • Ngân sách ~350.000 đ • Gu Cay nồng Đậm đà",
                "🍻 6 Người • Ngân sách ~600.000 đ • Gu Chiên giòn & Nướng",
                "🥗 3 Người • Ngân sách ~250.000 đ • Gu Thanh mát Chống ngấy"
        };

        new AlertDialog.Builder(this)
                .setTitle("🍱 Thiết Kế Mâm Cơm Theo Ngân Sách")
                .setItems(presetConfigs, (d, which) -> {
                    int guests = 2;
                    long budget = 200000L;
                    String style = "ALL";
                    if (which == 1) { guests = 4; budget = 400000L; style = "ALL"; }
                    else if (which == 2) { guests = 4; budget = 350000L; style = "SPICY"; }
                    else if (which == 3) { guests = 6; budget = 600000L; style = "CRISPY"; }
                    else if (which == 4) { guests = 3; budget = 250000L; style = "LIGHT"; }

                    showGeneratedComboProposalsDialog(guests, budget, style);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showGeneratedComboProposalsDialog(int guests, long budget, String style) {
        List<SmartComboProposal> proposals = recService.generateSmartCombos(guests, budget, style);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(36, 24, 36, 24);
        scrollView.addView(container);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("✨ Đề Xuất Mâm Cơm (" + guests + " người)")
                .setView(scrollView)
                .setNegativeButton("Đóng", null)
                .create();

        for (SmartComboProposal combo : proposals) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(24, 20, 24, 20);
            card.setBackgroundResource(R.drawable.bg_card_food);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 10, 0, 10);
            card.setLayoutParams(lp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(combo.getComboName());
            tvTitle.setTextSize(14f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.primary_orange));

            TextView tvRationale = new TextView(this);
            tvRationale.setText(combo.getRationale());
            tvRationale.setTextSize(11f);
            tvRationale.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            tvRationale.setPadding(0, 4, 0, 8);

            StringBuilder dishesSb = new StringBuilder();
            for (CartItem ci : combo.getItems()) {
                dishesSb.append(" • ").append(ci.getMenuItem().getName())
                        .append(" ×").append(ci.getQuantity())
                        .append(" (").append(String.format(Locale.getDefault(), "%,d đ", ci.getSubtotal())).append(")\n");
            }
            dishesSb.append(String.format(
                    Locale.getDefault(),
                    "🥗 Tổng năng lượng: ~%d kcal • 💰 Tổng giá trị: %,d đ\n🎟️ Tặng kèm mã CHIEUTHU3 giảm ngay tới 15%%!",
                    combo.getTotalCaloriesKcal(),
                    combo.getOriginalTotal()
            ));

            TextView tvDishes = new TextView(this);
            tvDishes.setText(dishesSb.toString().trim());
            tvDishes.setTextSize(12f);
            tvDishes.setTextColor(Color.BLACK);

            MaterialButton btnApplyCombo = new MaterialButton(this);
            btnApplyCombo.setText("🛒 Thêm Trọn Bộ Mâm Cơm Vào Giỏ");
            btnApplyCombo.setAllCaps(false);
            btnApplyCombo.setTextSize(12f);
            btnApplyCombo.setOnClickListener(v -> {
                for (CartItem ci : combo.getItems()) {
                    CartItem added = cartService.addItem(ci.getMenuItem(), ci.getQuantity());
                    if (!ci.getNote().isEmpty()) {
                        cartService.updateNote(added.getMenuItem().getId(), ci.getNote());
                    }
                }
                if (cartService.getAppliedVoucherCode().isEmpty()) {
                    cartService.setAppliedVoucherCode("CHIEUTHU3");
                }
                updateCartBadge();
                refreshRecommendations();
                dialog.dismiss();
                Toast.makeText(
                        MainActivity.this,
                        "🎉 Đã thêm '" + combo.getComboName() + "' và kích hoạt mã giảm giá CHIEUTHU3!",
                        Toast.LENGTH_LONG
                ).show();
                showCartBottomSheet();
            });

            card.addView(tvTitle);
            card.addView(tvRationale);
            card.addView(tvDishes);
            card.addView(btnApplyCombo);
            container.addView(card);
        }

        dialog.show();
    }

    /**
     * Tính năng mới 3: Kho Mã Giảm Giá / Voucher Khuyến Mãi
     */
    private void showVoucherWalletDialog(Runnable onVoucherChanged) {
        List<PromoVoucher> vouchers = opsService.getActiveVouchers();
        long subtotal = cartService.getTotalAmount();
        int itemCount = cartService.getTotalItemCount();

        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(36, 24, 36, 24);
        scrollView.addView(container);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🎟️ Kho Voucher & Ưu Đãi Nhà Hàng")
                .setView(scrollView)
                .setNeutralButton("Bỏ dùng mã", (d, w) -> {
                    cartService.setAppliedVoucherCode("");
                    if (onVoucherChanged != null) onVoucherChanged.run();
                    Toast.makeText(MainActivity.this, "Đã gỡ mã giảm giá khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .create();

        for (PromoVoucher v : vouchers) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(24, 18, 24, 18);
            card.setBackgroundResource(R.drawable.bg_card_food);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 8, 0, 8);
            card.setLayoutParams(lp);

            boolean eligible = v.isEligible(subtotal, itemCount);
            long estDiscount = v.calculateDiscount(subtotal, itemCount);

            TextView tvCode = new TextView(this);
            tvCode.setText("🏷️ [" + v.getCode() + "] " + v.getTitle());
            tvCode.setTextSize(13f);
            tvCode.setTypeface(null, android.graphics.Typeface.BOLD);
            tvCode.setTextColor(Color.parseColor("#DC2626"));

            TextView tvDesc = new TextView(this);
            String statusLine = eligible
                    ? String.format(Locale.getDefault(), "✅ Đủ điều kiện • Giảm ngay: -%,d đ", estDiscount)
                    : String.format(Locale.getDefault(), "ℹ️ Cần đơn tối thiểu %,d đ (%d món)", v.getMinOrderAmount(), v.getMinItemsCount());
            tvDesc.setText(v.getDescription() + "\n" + statusLine);
            tvDesc.setTextSize(11f);
            tvDesc.setTextColor(Color.BLACK);
            tvDesc.setPadding(0, 4, 0, 8);

            MaterialButton btnUse = new MaterialButton(this);
            boolean isCurrent = v.getCode().equalsIgnoreCase(cartService.getAppliedVoucherCode());
            btnUse.setText(isCurrent ? "✔ Đang Áp Dụng Mã Này" : "Áp Dụng Mã " + v.getCode());
            btnUse.setAllCaps(false);
            btnUse.setTextSize(11f);
            btnUse.setOnClickListener(view -> {
                cartService.setAppliedVoucherCode(v.getCode());
                if (onVoucherChanged != null) onVoucherChanged.run();
                dialog.dismiss();
                Toast.makeText(
                        MainActivity.this,
                        "🎟️ Đã chọn mã giảm giá " + v.getCode() + " (" + v.getTitle() + ")!",
                        Toast.LENGTH_SHORT
                ).show();
            });

            card.addView(tvCode);
            card.addView(tvDesc);
            card.addView(btnUse);
            container.addView(card);
        }

        dialog.show();
    }

    /**
     * Tính năng mới 4: Gọi Nhân Viên / Hỗ Trợ Tại Bàn 1 Chạm (Smart Waiter Call)
     */
    private void showCallWaiterDialog() {
        String currentTable = "Bàn " + opsService.getSelectedTableNumber();
        String[] serviceOptions = new String[]{
                "🧊 Thêm đá viên / Thêm trà đá miễn phí",
                "🥣 Thêm chén, đũa, muỗng, khăn giấy ướt",
                "🌶️ Thêm gia vị (Nước mắm ớt, tương ớt, muối tiêu chanh)",
                "🧹 Nhờ nhân viên dọn đĩa trống / Lau bàn",
                "🔥 Kiểm tra món đang chế biến giúp bàn",
                "💳 Nhờ thu ngân mang máy POS / Hóa đơn ra bàn"
        };

        new AlertDialog.Builder(this)
                .setTitle("🛎️ Gọi Phục Vụ Tại " + currentTable)
                .setItems(serviceOptions, (d, which) -> {
                    TableServiceRequest req = opsService.createServiceRequest(
                            currentTable,
                            serviceOptions[which],
                            "Khách gọi trực tiếp từ ứng dụng tại " + currentTable
                    );
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("✅ Đã Gửi Yêu Cầu Tới Quầy Phục Vụ!")
                            .setMessage(
                                    "• Bàn yêu cầu: " + req.getTableNumber()
                                            + "\n• Nội dung: " + req.getRequestType()
                                            + "\n• Trạng thái: Nhân viên phục vụ đã nhận thông báo và sẽ có mặt tại bàn trong ít phút!"
                            )
                            .setPositiveButton("Đã Hiểu", null)
                            .show();
                })
                .setNegativeButton("Đóng", null)
                .show();
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
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    /**
     * Hình 24: Giao diện Hồ Sơ Khách Hàng + Hạng Thành Viên VIP & Điểm Tích Lũy + UC-06: Theo dõi & Đặt lại đơn
     */
    private void showCustomerProfileDialog(Customer initialCustomer) {
        DialogCustomerProfileBinding dialogBinding = DialogCustomerProfileBinding.inflate(LayoutInflater.from(this));
        VietnameseTelexHelper.attachToEditText(dialogBinding.etProfileFullName, null);
        VietnameseTelexHelper.attachToEditText(dialogBinding.etProfileAddress, null);
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

            LoyaltyProfile loyalty = opsService.getCustomerLoyaltyProfile(c.getId());
            dialogBinding.tvProfileLoyaltyTier.setText(loyalty.getTierName());
            dialogBinding.tvProfileLoyaltyPoints.setText(
                    String.format(
                            Locale.getDefault(),
                            "⭐ %d điểm • Ưu đãi VIP -%d%%",
                            loyalty.getPoints(),
                            loyalty.getDiscountPercentBonus()
                    )
            );
            dialogBinding.progressLoyaltyTier.setProgress(loyalty.getProgressPercent());
            if (loyalty.getAmountToNextTier() > 0L) {
                dialogBinding.tvProfileLoyaltyNextHint.setText(
                        String.format(
                                Locale.getDefault(),
                                "Đã chi tiêu: %,d đ (%d đơn) • Còn %,d đ để thăng hạng %s",
                                loyalty.getTotalSpent(),
                                loyalty.getCompletedOrdersCount(),
                                loyalty.getAmountToNextTier(),
                                loyalty.getNextTierName()
                        )
                );
            } else {
                dialogBinding.tvProfileLoyaltyNextHint.setText(
                        String.format(
                                Locale.getDefault(),
                                "Đã chi tiêu: %,d đ (%d đơn) • Bạn đang ở hạng VIP cao nhất!",
                                loyalty.getTotalSpent(),
                                loyalty.getCompletedOrdersCount()
                        )
                );
            }
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
                        refreshRecommendations();
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
                                bindProfileHeaderAndForm.run();
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
            refreshRecommendations();
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
                refreshRecommendations();
                calculateCartTotal(dialogBinding);

                if (items.isEmpty()) {
                    dialogBinding.tvEmptyCartHint.setVisibility(View.VISIBLE);
                    dialogBinding.rvCartItems.setVisibility(View.GONE);
                    dialogBinding.layoutCartUpsellSection.setVisibility(View.GONE);
                } else {
                    dialogBinding.tvEmptyCartHint.setVisibility(View.GONE);
                    dialogBinding.rvCartItems.setVisibility(View.VISIBLE);

                    List<MenuItem> currentDishes = new ArrayList<>();
                    for (CartItem ci : items) {
                        currentDishes.add(ci.getMenuItem());
                    }
                    List<Recommendation> upsells = recService.getCartComplementaryRecommendations(currentDishes, 2);
                    dialogBinding.layoutCartUpsellItems.removeAllViews();
                    if (upsells.isEmpty()) {
                        dialogBinding.layoutCartUpsellSection.setVisibility(View.GONE);
                    } else {
                        dialogBinding.layoutCartUpsellSection.setVisibility(View.VISIBLE);
                        for (Recommendation rec : upsells) {
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
                            dialogBinding.layoutCartUpsellItems.addView(row);
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

        dialogBinding.btnCartSelectVoucher.setOnClickListener(v -> {
            showVoucherWalletDialog(refreshCartSheetState);
        });

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
        long subtotal = cartService.getTotalAmount();
        int itemCount = cartService.getTotalItemCount();

        int totalCalories = 0;
        for (CartItem ci : cartService.getCartItems()) {
            totalCalories += recService.estimateDishCalories(ci.getMenuItem()) * ci.getQuantity();
        }
        int suggestedPeople = Math.max(1, (totalCalories + 450) / 600);
        dialogBinding.tvCartNutritionSummary.setText(
                String.format(
                        Locale.getDefault(),
                        "🥗 Tổng năng lượng ước tính: ~%d kcal • Phù hợp ~%d người dùng bữa",
                        totalCalories,
                        suggestedPeople
                )
        );

        String voucherCode = cartService.getAppliedVoucherCode();
        long voucherDiscount = opsService.calculateVoucherDiscount(voucherCode, subtotal, itemCount);

        Customer currentCustomer = authService.getCurrentCustomer();
        int vipBonusPct = currentCustomer != null
                ? opsService.getCustomerLoyaltyProfile(currentCustomer.getId()).getDiscountPercentBonus()
                : 0;
        long vipDiscount = (subtotal * vipBonusPct) / 100L;
        long totalDiscount = Math.min(subtotal, voucherDiscount + vipDiscount);
        long payable = Math.max(0L, subtotal - totalDiscount);

        if (!voucherCode.isEmpty()) {
            PromoVoucher pv = opsService.getVoucherByCode(voucherCode);
            if (pv != null && voucherDiscount > 0L) {
                dialogBinding.tvCartVoucherStatus.setText("🎟️ Đang dùng mã " + voucherCode + " (-" + String.format(Locale.getDefault(), "%,d đ", voucherDiscount) + ")");
            } else {
                dialogBinding.tvCartVoucherStatus.setText("⚠️ Mã " + voucherCode + " chưa đủ điều kiện đơn tối thiểu");
            }
        } else {
            dialogBinding.tvCartVoucherStatus.setText("🎟️ Chưa chọn mã giảm giá (Chạm 'Chọn Mã' để giảm tới 20%)");
        }

        if (totalDiscount > 0L) {
            dialogBinding.tvCartDiscountInfo.setVisibility(View.VISIBLE);
            dialogBinding.tvCartDiscountInfo.setText(
                    String.format(Locale.getDefault(), "🎉 Ưu đãi Voucher & VIP: -%,d đ (Tạm tính: %,d đ)", totalDiscount, subtotal)
            );
        } else {
            dialogBinding.tvCartDiscountInfo.setVisibility(View.GONE);
        }

        dialogBinding.tvCartTotalPrice.setText(String.format(Locale.getDefault(), "%,d đ", payable));
    }

    private void showPaymentDialog() {
        if (cartService.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống! Vui lòng chọn món trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> currentCartItems = cartService.getCartItems();
        long subtotalAmount = cartService.getTotalAmount();
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
            String noteSuffix = !ci.getNote().isEmpty() ? " [" + ci.getNote() + "]" : "";
            tvLeft.setText((i + 1) + ". " + ci.getMenuItem().getName() + " (x" + ci.getQuantity() + ")" + noteSuffix);
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

        // 2. Tự động điền số bàn từ Sơ đồ bàn hoặc hồ sơ khách hàng
        Customer currentCustomer = authService.getCurrentCustomer();
        String defaultTable = opsService.getSelectedTableNumber();
        if (currentCustomer != null && currentCustomer.getAddress() != null) {
            String digits = currentCustomer.getAddress().replaceAll("[^0-9]", "");
            if (!digits.isEmpty() && digits.length() <= 3) {
                defaultTable = digits;
            }
        }
        dialogBinding.etTableNumber.setText(defaultTable);

        final int[] splitGuestHolder = new int[]{1};
        final long[] finalPayableHolder = new long[]{subtotalAmount};

        java.util.concurrent.Callable<String> getSelectedPaymentMethod = () -> {
            int checkedId = dialogBinding.chipGroupPaymentMethod.getCheckedChipId();
            if (checkedId == R.id.chipPayCash) {
                return "Tiền mặt tại bàn";
            } else if (checkedId == R.id.chipPayWallet) {
                return "Ví MoMo / ZaloPay";
            }
            return "Chuyển khoản QR";
        };

        // Hàm tính tiền thừa trả khách theo thời gian thực khi chọn Tiền mặt
        Runnable updateCashChange = () -> {
            long payable = finalPayableHolder[0];
            String rawCash = dialogBinding.etCashGiven.getText() != null
                    ? dialogBinding.etCashGiven.getText().toString().trim()
                    : "";
            long cashGiven = 0L;
            try {
                cashGiven = Long.parseLong(rawCash);
            } catch (NumberFormatException ignored) {}

            if (cashGiven >= payable) {
                long change = cashGiven - payable;
                dialogBinding.tvCashChangeAmount.setText(
                        String.format(Locale.getDefault(), "💰 Tiền thừa trả lại khách: %,d đ", change)
                );
                dialogBinding.tvCashChangeAmount.setTextColor(Color.parseColor("#15803D"));
            } else {
                long missing = payable - cashGiven;
                dialogBinding.tvCashChangeAmount.setText(
                        String.format(Locale.getDefault(), "⚠️ Khách đưa còn thiếu: %,d đ", missing)
                );
                dialogBinding.tvCashChangeAmount.setTextColor(Color.parseColor("#DC2626"));
            }
        };

        // Hàm cập nhật giao diện động theo Số bàn, Mã giảm giá, Chia tiền & Phương thức thanh toán
        Runnable updatePaymentUiState = () -> {
            String code = cartService.getAppliedVoucherCode();
            long voucherDisc = opsService.calculateVoucherDiscount(code, subtotalAmount, itemCount);
            int vipPct = currentCustomer != null
                    ? opsService.getCustomerLoyaltyProfile(currentCustomer.getId()).getDiscountPercentBonus()
                    : 0;
            long vipDisc = (subtotalAmount * vipPct) / 100L;
            long totalDisc = Math.min(subtotalAmount, voucherDisc + vipDisc);
            long payable = Math.max(0L, subtotalAmount - totalDisc);
            finalPayableHolder[0] = payable;

            StringBuilder discLabel = new StringBuilder("🎟️ Ưu đãi: -")
                    .append(String.format(Locale.getDefault(), "%,d đ", totalDisc));
            if (!code.isEmpty() && voucherDisc > 0L) {
                discLabel.append(" [Mã ").append(code).append("]");
            }
            if (vipPct > 0) {
                discLabel.append(" [VIP -").append(vipPct).append("%]");
            }
            dialogBinding.tvPaymentDiscountBreakdown.setText(discLabel.toString());
            dialogBinding.tvPaymentTotalAmount.setText(String.format(Locale.getDefault(), "%,d đ", payable));

            int guests = splitGuestHolder[0];
            long perPerson = (payable + guests - 1) / guests;
            dialogBinding.tvSplitGuestCount.setText(String.valueOf(guests));
            dialogBinding.tvSplitAmountPerPerson.setText(
                    String.format(Locale.getDefault(), "👥 Chia tiền (%d người): %,d đ/người", guests, perPerson)
            );

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
                    "Hóa đơn %s • Thanh toán: %,d đ (%d món) • %s",
                    tableLabel,
                    payable,
                    itemCount,
                    method
            ));

            String transferNote = "TT BAN " + cleanTable + " " + (payable / 1000) + "K";
            dialogBinding.tvTransferContent.setText("📝 Nội dung: " + transferNote);

            int checkedId = dialogBinding.chipGroupPaymentMethod.getCheckedChipId();
            if (checkedId == R.id.chipPayCash) {
                dialogBinding.layoutQrPaymentSection.setVisibility(View.GONE);
                dialogBinding.layoutCashPaymentSection.setVisibility(View.VISIBLE);
                dialogBinding.btnConfirmPayment.setText("✅ Xác Nhận Đặt Hàng (Thu Tiền Mặt Tại Bàn)");
            } else if (checkedId == R.id.chipPayWallet) {
                dialogBinding.layoutQrPaymentSection.setVisibility(View.VISIBLE);
                dialogBinding.layoutCashPaymentSection.setVisibility(View.GONE);
                dialogBinding.tvQrBoxLabel.setText("Mã QR Ví Điện Tử MoMo / ZaloPay (Chuẩn EMVCo)");
                dialogBinding.tvBankProviderName.setText("📱 Ví điện tử: MoMo / ZaloPay Business");
                dialogBinding.tvBankAccountOwner.setText("👤 Chủ ví nhận: NHA HANG CHIEU THU 3");
                dialogBinding.tvBankAccountNumber.setText("💳 SĐT Ví: 0901234567");
                dialogBinding.btnConfirmPayment.setText("✅ Xác Nhận Đã Thanh Toán Ví & Đặt Món");

                String emvPayload = QrBitmapGenerator.buildVietQrEmvPayload("970422", "0901234567", payable, transferNote);
                dialogBinding.imgQrCode.setImageBitmap(QrBitmapGenerator.generateQrBitmap(emvPayload));
            } else {
                dialogBinding.layoutQrPaymentSection.setVisibility(View.VISIBLE);
                dialogBinding.layoutCashPaymentSection.setVisibility(View.GONE);
                dialogBinding.tvQrBoxLabel.setText("Mã VietQR Napas247 (Quét Bằng App Ngân Hàng)");
                dialogBinding.tvBankProviderName.setText("🏦 Ngân hàng: MB Bank (Ngân hàng Quân Đội - BIN 970422)");
                dialogBinding.tvBankAccountOwner.setText("👤 Chủ tài khoản: NHA HANG CHIEU THU 3");
                dialogBinding.tvBankAccountNumber.setText("💳 Số TK: 0901234567");
                dialogBinding.btnConfirmPayment.setText("✅ Xác Nhận Đã Chuyển Khoản & Đặt Món");

                String emvPayload = QrBitmapGenerator.buildVietQrEmvPayload("970422", "0901234567", payable, transferNote);
                dialogBinding.imgQrCode.setImageBitmap(QrBitmapGenerator.generateQrBitmap(emvPayload));
            }
            updateCashChange.run();
        };

        updatePaymentUiState.run();
        dialogBinding.etCashGiven.setText(String.valueOf(finalPayableHolder[0]));

        dialogBinding.btnPaymentPickVoucher.setOnClickListener(v -> {
            showVoucherWalletDialog(() -> {
                updatePaymentUiState.run();
                dialogBinding.etCashGiven.setText(String.valueOf(finalPayableHolder[0]));
            });
        });

        dialogBinding.btnSplitMinus.setOnClickListener(v -> {
            if (splitGuestHolder[0] > 1) {
                splitGuestHolder[0]--;
                updatePaymentUiState.run();
            }
        });

        dialogBinding.btnSplitPlus.setOnClickListener(v -> {
            if (splitGuestHolder[0] < 20) {
                splitGuestHolder[0]++;
                updatePaymentUiState.run();
            }
        });

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
        dialogBinding.btnCashExact.setOnClickListener(v -> dialogBinding.etCashGiven.setText(String.valueOf(finalPayableHolder[0])));
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
            String transferNote = "TT BAN " + (tableRaw.isEmpty() ? "12" : tableRaw) + " " + (finalPayableHolder[0] / 1000) + "K";
            copyToClipboard("Nội dung chuyển khoản", transferNote);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnOpenQrUrl.setOnClickListener(v -> {
            String tableRaw = dialogBinding.etTableNumber.getText() != null
                    ? dialogBinding.etTableNumber.getText().toString().trim()
                    : "12";
            String url = "https://img.vietqr.io/image/MB-0901234567-compact2.png?amount=" + finalPayableHolder[0]
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

            long payable = finalPayableHolder[0];
            long discountSaved = Math.max(0L, subtotalAmount - payable);
            String usedVoucher = cartService.getAppliedVoucherCode();
            if (!usedVoucher.isEmpty() && discountSaved > 0L) {
                paymentMethod = paymentMethod + " (Mã " + usedVoucher + ")";
            }

            long cashGiven = payable;
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
                if (cashGiven < payable) {
                    Toast.makeText(
                            MainActivity.this,
                            "Số tiền khách đưa chưa đủ để thanh toán hóa đơn (" + String.format(Locale.getDefault(), "%,d đ", payable) + ")!",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                cashChange = cashGiven - payable;
            }

            String tableLabel = "Bàn " + tableRaw;
            opsService.occupyTable(tableLabel, splitGuestHolder[0]);
            updateTableButtonLabel();

            Customer customer = authService.getCurrentCustomer();
            Integer customerId = customer != null ? customer.getId() : null;

            Order createdOrder = orderService.createOrder(tableLabel, customerId, cartService.getCartItems(), paymentMethod);

            cartService.clearCart();
            updateCartBadge();
            refreshFoodList();
            refreshRecommendations();
            dialog.dismiss();

            showPaymentSuccessReceiptDialog(createdOrder, payable, discountSaved, cashGiven, cashChange);
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
    private void showPaymentSuccessReceiptDialog(Order order, long finalPaid, long discountSaved, long cashGiven, long cashChange) {
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
        if (discountSaved > 0L) {
            receipt.append("\n🎟️ Tiết kiệm Voucher/VIP: -").append(String.format(Locale.getDefault(), "%,d đ", discountSaved));
        }
        receipt.append("\n💰 THỰC THU: ").append(String.format(Locale.getDefault(), "%,d đ", finalPaid)).append("\n");
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

    private void showChatDialog() {
        DialogChatBinding dialogBinding = DialogChatBinding.inflate(LayoutInflater.from(this));

        ChatAdapter chatAdapter = new ChatAdapter(
                chatService.getChatHistory(),
                item -> {
                    cartService.addItem(item, 1);
                    updateCartBadge();
                    refreshRecommendations();
                    Toast.makeText(MainActivity.this, "Đã thêm '" + item.getName() + "' vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
        );
        dialogBinding.rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.rvChatMessages.setAdapter(chatAdapter);

        Runnable refreshAndScrollChat = () -> {
            chatAdapter.updateList(chatService.getChatHistory());
            if (!chatService.getChatHistory().isEmpty()) {
                dialogBinding.rvChatMessages.scrollToPosition(chatService.getChatHistory().size() - 1);
            }
        };
        refreshAndScrollChat.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        activeChatRefreshRunnable = refreshAndScrollChat;
        dialog.setOnDismissListener(d -> {
            if (activeChatRefreshRunnable == refreshAndScrollChat) {
                activeChatRefreshRunnable = null;
            }
        });

        dialogBinding.btnCloseChat.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.tvClearChatHistory.setOnClickListener(v -> {
            chatService.clearChat();
            refreshAndScrollChat.run();
            Toast.makeText(MainActivity.this, "Đã làm mới cuộc trò chuyện!", Toast.LENGTH_SHORT).show();
        });

        dialogBinding.chipQuickSpicyBudget.setOnClickListener(v -> {
            chatService.sendMessage("Tìm món cay ngon dưới 100k");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickBestseller.setOnClickListener(v -> {
            chatService.sendMessage("Gợi ý món đặc sản bán chạy nhất của quán");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickCombo.setOnClickListener(v -> {
            chatService.sendMessage("Đi 4 người nên gọi combo món gì");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickCrispy.setOnClickListener(v -> {
            chatService.sendMessage("Mình muốn ăn món nướng và món giòn rụm");
            refreshAndScrollChat.run();
        });
        dialogBinding.chipQuickDrinks.setOnClickListener(v -> {
            chatService.sendMessage("Cho xem danh mục nước uống giải khát");
            refreshAndScrollChat.run();
        });

        dialogBinding.btnChatCamera.setOnClickListener(v -> showImageRecognitionDialog());

        final boolean[] telexEnabledHolder = new boolean[]{true};
        VietnameseTelexHelper.attachToEditText(dialogBinding.etChatMessage, telexEnabledHolder);

        dialogBinding.tvToggleTelexMode.setOnClickListener(v -> {
            telexEnabledHolder[0] = !telexEnabledHolder[0];
            if (telexEnabledHolder[0]) {
                dialogBinding.tvToggleTelexMode.setText("🇻🇳 Gõ Telex: BẬT");
                dialogBinding.tvToggleTelexMode.setTextColor(Color.parseColor("#2E7D32"));
                Toast.makeText(MainActivity.this, "Đã bật bộ gõ Tiếng Việt có dấu (Telex)!", Toast.LENGTH_SHORT).show();
            } else {
                dialogBinding.tvToggleTelexMode.setText("🔤 Gõ Telex: TẮT");
                dialogBinding.tvToggleTelexMode.setTextColor(Color.parseColor("#757575"));
                Toast.makeText(MainActivity.this, "Đã tắt bộ gõ Telex!", Toast.LENGTH_SHORT).show();
            }
        });

        Runnable sendCurrentInput = () -> {
            String userText = dialogBinding.etChatMessage.getText() != null
                    ? dialogBinding.etChatMessage.getText().toString().trim()
                    : "";
            if (telexEnabledHolder[0] && !userText.isEmpty()) {
                userText = VietnameseTelexHelper.convertTelexSentence(userText);
            }
            chatService.sendMessage(userText);
            if (dialogBinding.etChatMessage.getText() != null) {
                dialogBinding.etChatMessage.getText().clear();
            }
            refreshAndScrollChat.run();
        };

        dialogBinding.btnSendChat.setOnClickListener(v -> sendCurrentInput.run());
        dialogBinding.etChatMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendCurrentInput.run();
                return true;
            }
            return false;
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
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
        String[] options = new String[]{
                "🖼️ Tải ảnh từ điện thoại (Thư viện / Tệp)",
                "📷 Chụp ảnh món ăn bằng Camera",
                "🍽️ Dùng ảnh mẫu thực đơn (Demo nhanh)"
        };

        new AlertDialog.Builder(this)
                .setTitle("📷 Nhận Diện Món Ăn Từ Ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickSearchImageLauncher.launch("image/*");
                    } else if (which == 1) {
                        launchCameraForFoodRecognition();
                    } else {
                        showSampleFoodImagePickerDialog();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void launchCameraForFoodRecognition() {
        File photoFile = FoodImageLoader.createTempCameraPhotoFile(this);
        if (photoFile == null) {
            Toast.makeText(this, "Không thể tạo tệp ảnh tạm cho Camera!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            pendingCameraPhotoFile = photoFile;
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            takeFoodPhotoLauncher.launch(photoUri);
        } catch (ActivityNotFoundException e) {
            pendingCameraPhotoFile = null;
            Toast.makeText(this, "Thiết bị chưa có ứng dụng Camera. Vui lòng chọn ảnh từ điện thoại!", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            pendingCameraPhotoFile = null;
            Toast.makeText(this, "Không thể khởi chạy Camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSampleFoodImagePickerDialog() {
        String[] sampleImages = getSampleFoodImages();
        new AlertDialog.Builder(this)
                .setTitle("🍽️ Chọn Ảnh Mẫu Thực Đơn")
                .setItems(sampleImages, (dialog, which) -> {
                    String selectedHint = sampleImages[which];
                    handleRecognizedFoodPhoto(selectedHint, selectedHint, null);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void handleRecognizedFoodPhoto(String displayLabel, String visualHint, String imageUri) {
        chatService.analyzeFoodImage(displayLabel, visualHint, imageUri);
        if (activeChatRefreshRunnable != null) {
            activeChatRefreshRunnable.run();
        } else {
            showChatDialog();
        }
    }
}
