package com.example.chieuthu3_ketthucmonhoc

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
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
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCustomerProfileBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogLoginBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogPaymentQrBinding
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult
import com.example.chieuthu3_ketthucmonhoc.domain.services.QrBitmapGenerator
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
        binding.btnHeaderUser.setOnClickListener { handleUserClick() }

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
                    handleUserClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateCartBadge() {
        val count = cartService.getTotalItemCount()
        binding.tvHeaderCartCount.text = String.format(Locale.getDefault(), "Giỏ (%d)", count)
    }

    private fun updateUserHeader() {
        val customer = authService.getCurrentCustomer()
        val admin = authService.getCurrentAdmin()
        when {
            customer != null -> binding.tvHeaderUserName.text = customer.fullName
            admin != null -> binding.tvHeaderUserName.text = "Admin: ${admin.fullName}"
            else -> binding.tvHeaderUserName.text = "Đăng Nhập"
        }
    }

    private fun handleUserClick() {
        val customer = authService.getCurrentCustomer()
        val admin = authService.getCurrentAdmin()
        when {
            customer != null -> showCustomerProfileDialog(customer)
            admin != null -> {
                AlertDialog.Builder(this)
                    .setTitle("🛠️ Phiên Quản Trị: ${admin.fullName}")
                    .setMessage("Bạn đang đăng nhập với quyền quản trị viên (${admin.role.displayName}).")
                    .setPositiveButton("Vào Quản Trị") { _, _ ->
                        startActivity(Intent(this, AdminActivity::class.java))
                    }
                    .setNegativeButton("Đăng Xuất") { _, _ ->
                        authService.logout()
                        updateUserHeader()
                        Toast.makeText(this, "Đã đăng xuất phiên quản trị!", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Đóng", null)
                    .show()
            }
            else -> showLoginDialog()
        }
    }

    private fun showCustomerProfileDialog(customer: Customer) {
        val dialogBinding = DialogCustomerProfileBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvProfileFullName.text = customer.fullName
        dialogBinding.tvProfilePhone.text = "Số điện thoại: ${customer.phoneNumber}"
        dialogBinding.tvProfileEmail.text = if (customer.email.isNotBlank()) "Email: ${customer.email}" else "Email: Chưa cập nhật"

        val orders = orderService.getCustomerOrders(customer.id)
        if (orders.isEmpty()) {
            dialogBinding.tvNoOrdersHint.visibility = View.VISIBLE
        } else {
            dialogBinding.tvNoOrdersHint.visibility = View.GONE
            for (order in orders.take(5)) {
                val orderItemView = android.widget.TextView(this).apply {
                    val statusText = when (order.status) {
                        OrderStatus.PENDING -> "Chờ xác nhận"
                        OrderStatus.PREPARING -> "Bếp đang nấu"
                        OrderStatus.COMPLETED -> "Đã hoàn thành ✓"
                        OrderStatus.CANCELLED -> "Đã hủy"
                    }
                    text = "${order.orderId} • ${order.formattedTotal} • $statusText\n(${order.items.joinToString { "${it.menuItem.name} x${it.quantity}" }})"
                    textSize = 12f
                    setPadding(24, 16, 24, 16)
                    setBackgroundResource(R.drawable.bg_card_food)
                    val params = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    layoutParams = params
                }
                dialogBinding.layoutCustomerOrders.addView(orderItemView)
            }
        }

        dialogBinding.btnLogoutCustomer.setOnClickListener {
            authService.logout()
            updateUserHeader()
            refreshAiRecommendations()
            dialog.dismiss()
            Toast.makeText(this, "Đã đăng xuất tài khoản!", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCloseProfile.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

        fun tableLabelFromInput(): String? {
            val tableRaw = dialogBinding.etTableNumber.text.toString().trim()
            if (tableRaw.isEmpty()) return null
            return "Bàn $tableRaw"
        }

        fun updateOrderSummary() {
            val tableLabel = tableLabelFromInput() ?: "Bàn ?"
            dialogBinding.tvOrderSummary.text = String.format(
                Locale.getDefault(),
                "Hóa đơn %s • Tổng cộng: %,d đ (%d món)",
                tableLabel,
                totalAmount,
                itemCount
            )
        }

        dialogBinding.etTableNumber.setText("12")
        updateOrderSummary()
        dialogBinding.etTableNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dialogBinding.tilTableNumber.error = null
                updateOrderSummary()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Đơn được tạo (PENDING) ngay khi khách yêu cầu mã QR; giỏ chỉ được xóa sau khi xác nhận thanh toán.
        var createdOrder: Order? = null

        fun createPendingOrderAndShowQr(): Order? {
            createdOrder?.let { return it }
            val tableLabel = tableLabelFromInput()
            if (tableLabel == null) {
                dialogBinding.tilTableNumber.error = "Vui lòng nhập số bàn trước khi thanh toán"
                return null
            }
            val customer = authService.getCurrentCustomer()
            val order = orderService.createOrder(
                tableNumber = tableLabel,
                customerId = customer?.id,
                cartItems = cartService.getCartItems()
            )
            createdOrder = order

            dialogBinding.tilTableNumber.isEnabled = false
            dialogBinding.imgQrCode.setImageBitmap(QrBitmapGenerator.generateQrBitmap(order.qrPayloadUrl.orEmpty()))
            dialogBinding.tvQrUrl.text = "Link VietQR: ${order.qrPayloadUrl}"
            dialogBinding.tvQrUrl.visibility = View.VISIBLE
            dialogBinding.btnOpenQrUrl.visibility = View.VISIBLE
            dialogBinding.btnCreateOrderQr.text = "Đã tạo đơn ${order.orderId} ✓"
            dialogBinding.btnCreateOrderQr.isEnabled = false
            return order
        }

        dialogBinding.btnCreateOrderQr.setOnClickListener {
            createPendingOrderAndShowQr()
        }

        dialogBinding.btnOpenQrUrl.setOnClickListener {
            val url = createdOrder?.qrPayloadUrl
            if (url.isNullOrBlank()) return@setOnClickListener
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Thiết bị không có trình duyệt để mở mã QR. Bạn có thể nhấn giữ link ở trên để sao chép.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialogBinding.btnConfirmPayment.setOnClickListener {
            val order = createdOrder ?: createPendingOrderAndShowQr() ?: return@setOnClickListener

            cartService.clearCart()
            updateCartBadge()
            refreshAiRecommendations()
            dialog.dismiss()

            Toast.makeText(
                this,
                "Thanh toán thành công đơn ${order.orderId} (${order.tableNumber})! Bếp đang chế biến món ăn.",
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
            "Chân Giò Muối Chiên Giòn (Món chiên đặc sản)",
            "Ếch Nướng Muối Ớt Lá Chanh & Gà Nướng Mật Ong (Món nướng)",
            "Chân Gà Sốt Thái & Gỏi Ngó Sen Tôm Thịt (Món phụ chua cay)",
            "Nước Ngọt Giải Khát (Coca Cola, 7Up, Pepsi, Sting)",
            "Lẩu Gà Lá Giang (Món lẩu đặc biệt)"
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
            val phone = dialogBinding.etPhone.text.toString().trim()
            val email = dialogBinding.etEmail.text.toString().trim()
            val password = dialogBinding.etPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ và tên khi đăng ký!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.length < 9) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại hợp lệ (tối thiểu 9 chữ số)!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
            val username = dialogBinding.etAdminUsername.text.toString().trim()
            val password = dialogBinding.etAdminPassword.text.toString()

            when (val result = authService.loginAdmin(username, password)) {
                is AuthResult.Success -> {
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "Đăng nhập quản trị thành công! Xin chào ${result.data.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, AdminActivity::class.java))
                }
                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}
