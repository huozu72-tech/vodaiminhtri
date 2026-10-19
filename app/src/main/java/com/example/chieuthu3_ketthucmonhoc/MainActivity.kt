package com.example.chieuthu3_ketthucmonhoc

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chieuthu3_ketthucmonhoc.adapters.AIRecommendationAdapter
import com.example.chieuthu3_ketthucmonhoc.adapters.CartAdapter
import com.example.chieuthu3_ketthucmonhoc.adapters.ChatAdapter
import com.example.chieuthu3_ketthucmonhoc.adapters.FoodAdapter
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityMainBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAiChatBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCartBottomSheetBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogLoginBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogPaymentQrBinding
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var aiRecAdapter: AIRecommendationAdapter

    // Services via AppServiceLocator (Clean OOP Architecture)
    private val menuService = AppServiceLocator.menuService
    private val cartService = AppServiceLocator.cartService
    private val orderService = AppServiceLocator.orderService
    private val authService = AppServiceLocator.authService
    private val aiRecService = AppServiceLocator.aiRecommendationService
    private val aiChatService = AppServiceLocator.aiChatbotService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupFoodGrid()
        setupAiRecommendations()
        setupSearchAndFilters()
        setupListeners()
        updateCartBadge()
        updateUserHeader()
    }

    override fun onResume() {
        super.onResume()
        refreshFoodList()
        refreshAiRecommendations()
        updateCartBadge()
        updateUserHeader()
    }

    private fun setupFoodGrid() {
        foodAdapter = FoodAdapter(menuService.getAvailableMenuItems()) { menuItem ->
            cartService.addItem(menuItem, 1)
            updateCartBadge()
            refreshAiRecommendations()
            Toast.makeText(this, "Đã thêm '${menuItem.name}' vào giỏ!", Toast.LENGTH_SHORT).show()
        }
        binding.rvFoodGrid.layoutManager = GridLayoutManager(this, 2)
        binding.rvFoodGrid.adapter = foodAdapter
    }

    private fun setupAiRecommendations() {
        val currentCustomerId = authService.getCurrentCustomer()?.id
        val recommendations = aiRecService.getPersonalizedRecommendations(currentCustomerId, limit = 5)
        aiRecAdapter = AIRecommendationAdapter(recommendations) { menuItem ->
            cartService.addItem(menuItem, 1)
            updateCartBadge()
            Toast.makeText(this, "Đã thêm '${menuItem.name}' vào giỏ!", Toast.LENGTH_SHORT).show()
        }
        binding.rvAiRecommendations.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAiRecommendations.adapter = aiRecAdapter
    }

    private fun refreshFoodList() {
        val currentCategory = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipMain -> FoodCategory.MAIN_DISH
            R.id.chipSide -> FoodCategory.SIDE_DISH
            R.id.chipDrink -> FoodCategory.BEVERAGE
            else -> null
        }
        val items = if (currentCategory != null) {
            menuService.getMenuItemsByCategory(currentCategory)
        } else {
            menuService.getAvailableMenuItems()
        }
        foodAdapter.updateList(items)
    }

    private fun refreshAiRecommendations() {
        val currentCustomerId = authService.getCurrentCustomer()?.id
        val updatedRecs = aiRecService.getPersonalizedRecommendations(currentCustomerId, limit = 5)
        aiRecAdapter.updateList(updatedRecs)
    }

    private fun setupSearchAndFilters() {
        binding.etSearchFood.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val query = s?.toString().orEmpty()
                if (query.isBlank()) {
                    refreshFoodList()
                } else {
                    val filtered = menuService.searchMenuItems(query).filter { it.isAvailable }
                    foodAdapter.updateList(filtered)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            binding.etSearchFood.text?.clear()
            when (checkedIds[0]) {
                R.id.chipMain -> foodAdapter.updateList(menuService.getMenuItemsByCategory(FoodCategory.MAIN_DISH))
                R.id.chipSide -> foodAdapter.updateList(menuService.getMenuItemsByCategory(FoodCategory.SIDE_DISH))
                R.id.chipDrink -> foodAdapter.updateList(menuService.getMenuItemsByCategory(FoodCategory.BEVERAGE))
                else -> foodAdapter.updateList(menuService.getAvailableMenuItems())
            }
        }
    }

    private fun setupListeners() {
        binding.btnHeaderCart.setOnClickListener { showCartBottomSheet() }
        binding.btnHeaderPayment.setOnClickListener { showPaymentDialog() }
        binding.btnHeaderUser.setOnClickListener { showLoginDialog() }

        binding.btnAiUploadPhoto.setOnClickListener { showImageRecognitionDialog() }
        binding.fabAiChatbot.setOnClickListener { showAiChatDialog() }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.chipAll.isChecked = true
                    foodAdapter.updateList(menuService.getAvailableMenuItems())
                    true
                }
                R.id.nav_ai -> {
                    showAiChatDialog()
                    true
                }
                R.id.nav_cart -> {
                    showCartBottomSheet()
                    true
                }
                R.id.nav_payment -> {
                    showPaymentDialog()
                    true
                }
                R.id.nav_profile -> {
                    showLoginDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateCartBadge() {
        val count = cartService.getTotalItemCount()
        binding.tvHeaderCartCount.text = String.format(Locale.getDefault(), "Giỏ Hàng (%d)", count)
    }

    private fun updateUserHeader() {
        val customer = authService.getCurrentCustomer()
        if (customer != null) {
            binding.tvHeaderUserName.text = customer.fullName
        } else {
            binding.tvHeaderUserName.text = "Đăng Nhập"
        }
    }

    private fun showCartBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val dialogBinding = DialogCartBottomSheetBinding.inflate(LayoutInflater.from(this))
        bottomSheet.setContentView(dialogBinding.root)

        lateinit var cartAdapter: CartAdapter
        cartAdapter = CartAdapter(
            cartList = cartService.getCartItems(),
            onQuantityPlus = { cartItem ->
                cartService.updateQuantity(cartItem.menuItem.id, cartItem.quantity + 1)
                cartAdapter.updateList(cartService.getCartItems())
                updateCartBadge()
                calculateCartTotal(dialogBinding)
            },
            onQuantityMinus = { cartItem ->
                cartService.updateQuantity(cartItem.menuItem.id, cartItem.quantity - 1)
                cartAdapter.updateList(cartService.getCartItems())
                updateCartBadge()
                calculateCartTotal(dialogBinding)
            }
        )
        dialogBinding.rvCartItems.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvCartItems.adapter = cartAdapter

        calculateCartTotal(dialogBinding)

        dialogBinding.tvClearCart.setOnClickListener {
            cartService.clearCart()
            cartAdapter.updateList(emptyList())
            updateCartBadge()
            calculateCartTotal(dialogBinding)
            Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng!", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCheckout.setOnClickListener {
            if (cartService.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show()
            } else {
                bottomSheet.dismiss()
                showPaymentDialog()
            }
        }

        bottomSheet.show()
    }

    private fun calculateCartTotal(dialogBinding: DialogCartBottomSheetBinding) {
        val total = cartService.getTotalAmount()
        dialogBinding.tvCartTotalPrice.text = String.format(Locale.getDefault(), "%,d đ", total)
    }

    private fun showPaymentDialog() {
        if (cartService.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống! Vui lòng chọn món trước.", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = cartService.getTotalAmount()
        val itemCount = cartService.getTotalItemCount()
        val dialogBinding = DialogPaymentQrBinding.inflate(LayoutInflater.from(this))

        dialogBinding.tvOrderSummary.text = String.format(
            Locale.getDefault(),
            "Hóa đơn Bàn 12 • Tổng cộng: %,d đ (%d món)",
            totalAmount,
            itemCount
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnConfirmPayment.setOnClickListener {
            val customer = authService.getCurrentCustomer()
            val order = orderService.createOrder(
                tableNumber = "Bàn 12",
                customerId = customer?.id,
                cartItems = cartService.getCartItems()
            )

            cartService.clearCart()
            updateCartBadge()
            refreshAiRecommendations()
            dialog.dismiss()

            Toast.makeText(
                this,
                "Thanh toán thành công đơn ${order.orderId}! Bếp đang chế biến món ăn.",
                Toast.LENGTH_LONG
            ).show()
        }

        dialog.show()
    }

    private fun showAiChatDialog() {
        val dialogBinding = DialogAiChatBinding.inflate(LayoutInflater.from(this))

        val chatAdapter = ChatAdapter(
            chatList = aiChatService.getChatHistory(),
            onAddSuggestedItemClick = { item ->
                cartService.addItem(item, 1)
                updateCartBadge()
                Toast.makeText(this, "Đã thêm '${item.name}' vào giỏ hàng!", Toast.LENGTH_SHORT).show()
            }
        )
        dialogBinding.rvChatMessages.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvChatMessages.adapter = chatAdapter
        dialogBinding.rvChatMessages.scrollToPosition(aiChatService.getChatHistory().size - 1)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCloseChat.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnChatCamera.setOnClickListener {
            val aiMsg = aiChatService.analyzeFoodImage("thực đơn món nướng và món chiên giòn")
            chatAdapter.updateList(aiChatService.getChatHistory())
            dialogBinding.rvChatMessages.scrollToPosition(aiChatService.getChatHistory().size - 1)
        }

        dialogBinding.btnSendChat.setOnClickListener {
            val userText = dialogBinding.etChatMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                aiChatService.sendMessage(userText)
                dialogBinding.etChatMessage.text?.clear()
                chatAdapter.updateList(aiChatService.getChatHistory())
                dialogBinding.rvChatMessages.scrollToPosition(aiChatService.getChatHistory().size - 1)
            }
        }

        dialog.show()
    }

    private fun showImageRecognitionDialog() {
        val sampleImages = arrayOf(
            "Ảnh 1: Món Chiên Giòn (Chân giò, Tôm chiên)",
            "Ảnh 2: Món Nướng Muối Ớt (Ếch nướng, Gà nướng, Tôm nướng)",
            "Ảnh 3: Món Gỏi Khai Vị & Chân Gà Sốt Thái",
            "Ảnh 4: Nước Ngọt Giải Khát (Coca, 7Up, Pepsi)"
        )

        AlertDialog.Builder(this)
            .setTitle("📷 Chọn Ảnh Món Ăn Cần Nhận Diện")
            .setItems(sampleImages) { _, which ->
                val selectedHint = sampleImages[which]
                aiChatService.analyzeFoodImage(selectedHint)
                showAiChatDialog()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun showLoginDialog() {
        val dialogBinding = DialogLoginBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnLoginSubmit.setOnClickListener {
            val phoneOrEmail = dialogBinding.etPhone.text.toString().trim()
            val password = dialogBinding.etPassword.text.toString().trim()

            when (val result = authService.loginCustomer(phoneOrEmail, password)) {
                is AuthResult.Success -> {
                    updateUserHeader()
                    refreshAiRecommendations()
                    dialog.dismiss()
                    Toast.makeText(this, "Đăng nhập thành công! Chào ${result.data.fullName}", Toast.LENGTH_SHORT).show()
                }
                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogBinding.btnRegisterSubmit.setOnClickListener {
            val fullName = dialogBinding.etFullName.text.toString().trim()
            val phoneOrEmail = dialogBinding.etPhone.text.toString().trim()
            val password = dialogBinding.etPassword.text.toString().trim()

            val email = if (phoneOrEmail.contains("@")) phoneOrEmail else ""
            val phone = if (!phoneOrEmail.contains("@")) phoneOrEmail else "090" + System.currentTimeMillis() % 10000000

            when (val result = authService.register(fullName, email, phone, password)) {
                is AuthResult.Success -> {
                    updateUserHeader()
                    refreshAiRecommendations()
                    dialog.dismiss()
                    Toast.makeText(this, "Đăng ký thành công! Chào ${result.data.fullName}", Toast.LENGTH_SHORT).show()
                }
                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogBinding.btnSwitchToAdmin.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AdminActivity::class.java))
        }

        dialog.show()
    }
}
