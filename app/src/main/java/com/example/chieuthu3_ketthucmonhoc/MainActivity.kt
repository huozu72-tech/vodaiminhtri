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
import com.example.chieuthu3_ketthucmonhoc.adapters.CartAdapter
import com.example.chieuthu3_ketthucmonhoc.adapters.ChatAdapter
import com.example.chieuthu3_ketthucmonhoc.adapters.FoodAdapter
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityMainBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAiChatBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogCartBottomSheetBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogLoginBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogPaymentQrBinding
import com.example.chieuthu3_ketthucmonhoc.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.models.ChatMessage
import com.example.chieuthu3_ketthucmonhoc.models.FoodItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var foodAdapter: FoodAdapter

    private val allFoodList = ArrayList<FoodItem>()
    private val cartList = ArrayList<CartItem>()
    private val chatList = ArrayList<ChatMessage>()

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

        initSampleData()
        setupFoodGrid()
        setupCategoryChips()
        setupListeners()
    }

    private fun initSampleData() {
        allFoodList.clear()
        // 15 Food items matching the provided screenshot
        allFoodList.add(FoodItem(1, "Chân Giò Muối Chiên Giòn", "Món chính", 120000, "120.000 đ", "Chân Giò Muối Chiên Giòn đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(2, "Ếch Nướng Muối Ớt Lá Chanh", "Món chính", 85000, "85.000 đ", "Ếch Nướng Muối Ớt Lá Chanh đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(3, "Gà Nướng Mật Ong", "Món chính", 150000, "150.000 đ", "Gà Nướng Mật Ong đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(4, "Gà Quay Bánh Bao", "Món chính", 160000, "160.000 đ", "Gà Quay Bánh Bao đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(5, "Tôm Chiên Xù", "Món chính", 95000, "95.000 đ", "Tôm Chiên Xù đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(6, "Tôm Nướng Muối Ớt", "Món chính", 105000, "105.000 đ", "Tôm Nướng Muối Ớt đậm đà hương vị truyền thống thơm ngon"))
        
        allFoodList.add(FoodItem(7, "Chân Gà Sả Tắc", "Món phụ", 60000, "60.000 đ", "Chân Gà Sả Tắc đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(8, "Chân Gà Sốt Thái", "Món phụ", 65000, "65.000 đ", "Chân Gà Sốt Thái đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(9, "Gỏi Ngó Sen", "Món phụ", 55000, "55.000 đ", "Gỏi Ngó Sen đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(10, "Gỏi Xoài Tôm Khô", "Món phụ", 70000, "70.000 đ", "Gỏi Xoài Tôm Khô đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(11, "Gỏi Xoài Tôm Thịt", "Món phụ", 80000, "80.000 đ", "Gỏi Xoài Tôm Thịt đậm đà hương vị truyền thống thơm ngon"))
        
        allFoodList.add(FoodItem(12, "7Up", "Nước uống", 15000, "15.000 đ", "7Up đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(13, "Coca Cola", "Nước uống", 15000, "15.000 đ", "Coca Cola đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(14, "Pepsi", "Nước uống", 15000, "15.000 đ", "Pepsi đậm đà hương vị truyền thống thơm ngon"))
        allFoodList.add(FoodItem(15, "Sting", "Nước uống", 15000, "15.000 đ", "Sting đậm đà hương vị truyền thống thơm ngon"))

        // Welcome chat message from AI
        chatList.add(ChatMessage("Xin chào! Tôi là AI Trợ Lý Đề Món. Bạn muốn tìm món ăn gì hôm nay?", isUser = false))
    }

    private fun setupFoodGrid() {
        foodAdapter = FoodAdapter(allFoodList) { food ->
            addToCart(food)
        }
        binding.rvFoodGrid.layoutManager = GridLayoutManager(this, 2)
        binding.rvFoodGrid.adapter = foodAdapter
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds[0]) {
                R.id.chipMain -> filterByCategory("Món chính")
                R.id.chipSide -> filterByCategory("Món phụ")
                R.id.chipDrink -> filterByCategory("Nước uống")
                else -> foodAdapter.updateList(allFoodList)
            }
        }
    }

    private fun filterByCategory(category: String) {
        val filtered = allFoodList.filter { it.category == category }
        foodAdapter.updateList(filtered)
    }

    private fun addToCart(food: FoodItem) {
        val existing = cartList.find { it.foodItem.id == food.id }
        if (existing != null) {
            existing.quantity++
        } else {
            cartList.add(CartItem(food, 1))
        }
        updateCartBadge()
        Toast.makeText(this, "Đã thêm '${food.name}' vào giỏ!", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartBadge() {
        val totalCount = cartList.sumOf { it.quantity }
        binding.tvHeaderCartCount.text = String.format(Locale.getDefault(), "Giỏ Hàng (%d)", totalCount)
    }

    private fun setupListeners() {
        binding.btnHeaderCart.setOnClickListener { showCartBottomSheet() }
        binding.btnHeaderPayment.setOnClickListener { showPaymentDialog() }
        binding.btnHeaderUser.setOnClickListener { showLoginDialog() }

        binding.btnAiUploadPhoto.setOnClickListener { showAiChatDialog() }
        binding.fabAiChatbot.setOnClickListener { showAiChatDialog() }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.chipAll.isChecked = true
                    foodAdapter.updateList(allFoodList)
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

    private fun showCartBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val dialogBinding = DialogCartBottomSheetBinding.inflate(LayoutInflater.from(this))
        bottomSheet.setContentView(dialogBinding.root)

        val cartAdapter = CartAdapter(cartList) {
            updateCartBadge()
            calculateCartTotal(dialogBinding)
        }
        dialogBinding.rvCartItems.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvCartItems.adapter = cartAdapter

        calculateCartTotal(dialogBinding)

        dialogBinding.tvClearCart.setOnClickListener {
            val count = cartList.size
            cartList.clear()
            cartAdapter.notifyItemRangeRemoved(0, count)
            updateCartBadge()
            calculateCartTotal(dialogBinding)
            Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng!", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCheckout.setOnClickListener {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show()
            } else {
                bottomSheet.dismiss()
                showPaymentDialog()
            }
        }

        bottomSheet.show()
    }

    private fun calculateCartTotal(dialogBinding: DialogCartBottomSheetBinding) {
        val totalSum = cartList.sumOf { it.foodItem.price * it.quantity }
        dialogBinding.tvCartTotalPrice.text = String.format(Locale.getDefault(), "%,d đ", totalSum)
    }

    private fun showPaymentDialog() {
        val totalSum = cartList.sumOf { it.foodItem.price * it.quantity }
        val dialogBinding = DialogPaymentQrBinding.inflate(LayoutInflater.from(this))
        
        dialogBinding.tvOrderSummary.text = String.format(
            Locale.getDefault(),
            "Hóa đơn Bàn 12 • Tổng cộng: %,d đ (%d món)",
            totalSum,
            cartList.sumOf { it.quantity },
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnConfirmPayment.setOnClickListener {
            Toast.makeText(this, "Thanh toán thành công! Đơn hàng đang được chế biến.", Toast.LENGTH_LONG).show()
            cartList.clear()
            updateCartBadge()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAiChatDialog() {
        val dialogBinding = DialogAiChatBinding.inflate(LayoutInflater.from(this))
        
        val chatAdapter = ChatAdapter(chatList)
        dialogBinding.rvChatMessages.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvChatMessages.adapter = chatAdapter
        dialogBinding.rvChatMessages.scrollToPosition(chatList.size - 1)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCloseChat.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnChatCamera.setOnClickListener {
            val startPos = chatList.size
            chatList.add(ChatMessage("📷 [Đã tải lên ảnh thực đơn món ăn]", isUser = true))
            chatList.add(ChatMessage("🤖 AI đã phân tích ảnh! Đề xuất cho bạn: 'Tôm Nướng Muối Ớt' và 'Chân Giò Muối Chiên Giòn' rất hợp khẩu vị hôm nay.", isUser = false))
            chatAdapter.notifyItemRangeInserted(startPos, 2)
            dialogBinding.rvChatMessages.scrollToPosition(chatList.size - 1)
        }

        dialogBinding.btnSendChat.setOnClickListener {
            val userText = dialogBinding.etChatMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                val startPos = chatList.size
                chatList.add(ChatMessage(userText, isUser = true))
                dialogBinding.etChatMessage.setText("")

                val aiResponse = when {
                    userText.contains("cay", ignoreCase = true) -> 
                        "🤖 Dựa trên yêu cầu món cay, AI gợi ý: 'Ếch Nướng Muối Ớt' (85k) hoặc 'Chân Gà Sốt Thái' (65k)!"
                    userText.contains("nước", ignoreCase = true) || userText.contains("uống", ignoreCase = true) ->
                        "🤖 Nước uống giải nhiệt tuyệt vời: Coca Cola, Pepsi, 7Up đồng giá 15.000 đ!"
                    else -> 
                        "🤖 AI khuyến nghị bạn thử 'Gà Nướng Mật Ong' (150k) - món bán chạy nhất của Nhà Hàng Việt!"
                }
                chatList.add(ChatMessage(aiResponse, isUser = false))
                chatAdapter.notifyItemRangeInserted(startPos, 2)
                dialogBinding.rvChatMessages.scrollToPosition(chatList.size - 1)
            }
        }

        dialog.show()
    }

    private fun showLoginDialog() {
        val dialogBinding = DialogLoginBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Real-time error validation
        dialogBinding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty() || s.length < 9) {
                    dialogBinding.tilPhone.error = "Số điện thoại / Email chưa đúng định dạng"
                } else {
                    dialogBinding.tilPhone.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialogBinding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty() || s.length < 6) {
                    dialogBinding.tilPassword.error = "Mật khẩu tối thiểu 6 ký tự"
                } else {
                    dialogBinding.tilPassword.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialogBinding.btnLoginSubmit.setOnClickListener {
            val phone = dialogBinding.etPhone.text.toString()
            val pass = dialogBinding.etPassword.text.toString()
            if (phone.length >= 9 && pass.length >= 6) {
                val userName = dialogBinding.etFullName.text.toString().ifEmpty { "1.viet" }
                binding.tvHeaderUserName.text = userName
                Toast.makeText(this, "Đăng nhập thành công! Chào $userName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Vui lòng nhập đúng thông tin tài khoản", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnSwitchToAdmin.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
    }
}
