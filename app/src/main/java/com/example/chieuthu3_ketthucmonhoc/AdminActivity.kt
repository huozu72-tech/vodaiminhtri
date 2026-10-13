package com.example.chieuthu3_ketthucmonhoc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chieuthu3_ketthucmonhoc.adapters.AdminFoodAdapter
import com.example.chieuthu3_ketthucmonhoc.adapters.AdminOrderAdapter
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityAdminBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.DialogAddEditFoodBinding
import com.example.chieuthu3_ketthucmonhoc.models.FoodItem
import com.example.chieuthu3_ketthucmonhoc.models.Order
import java.util.Locale

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adminFoodAdapter: AdminFoodAdapter
    private lateinit var adminOrderAdapter: AdminOrderAdapter

    private val adminFoodList = ArrayList<FoodItem>()
    private val adminOrderList = ArrayList<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initSampleData()
        setupTabs()
        setupFoodRecyclerView()
        setupOrderRecyclerView()
        setupListeners()
    }

    private fun initSampleData() {
        // Sample food list for Admin
        adminFoodList.clear()
        adminFoodList.add(FoodItem(1, "Chân Giò Muối Chiên Giòn", "Món chính", 120000, "120.000 đ", "Chân Giò Muối Chiên Giòn đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(2, "Ếch Nướng Muối Ớt Lá Chanh", "Món chính", 85000, "85.000 đ", "Ếch Nướng Muối Ớt Lá Chanh đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(3, "Gà Nướng Mật Ong", "Món chính", 150000, "150.000 đ", "Gà Nướng Mật Ong đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(4, "Gà Quay Bánh Bao", "Món chính", 160000, "160.000 đ", "Gà Quay Bánh Bao đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(5, "Tôm Chiên Xù", "Món chính", 95000, "95.000 đ", "Tôm Chiên Xù đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(6, "Tôm Nướng Muối Ớt", "Món chính", 105000, "105.000 đ", "Tôm Nướng Muối Ớt đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(7, "Chân Gà Sả Tắc", "Món phụ", 60000, "60.000 đ", "Chân Gà Sả Tắc đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(8, "Chân Gà Sốt Thái", "Món phụ", 65000, "65.000 đ", "Chân Gà Sốt Thái đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(9, "Gỏi Ngó Sen", "Món phụ", 55000, "55.000 đ", "Gỏi Ngó Sen đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(10, "Gỏi Xoài Tôm Khô", "Món phụ", 70000, "70.000 đ", "Gỏi Xoài Tôm Khô đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(11, "Gỏi Xoài Tôm Thịt", "Món phụ", 80000, "80.000 đ", "Gỏi Xoài Tôm Thịt đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(12, "7Up", "Nước uống", 15000, "15.000 đ", "7Up đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(13, "Coca Cola", "Nước uống", 15000, "15.000 đ", "Coca Cola đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(14, "Pepsi", "Nước uống", 15000, "15.000 đ", "Pepsi đậm đà hương vị truyền thống thơm ngon"))
        adminFoodList.add(FoodItem(15, "Sting", "Nước uống", 15000, "15.000 đ", "Sting đậm đà hương vị truyền thống thơm ngon"))

        // Sample orders matching Screenshot
        adminOrderList.clear()
        adminOrderList.add(Order("#ORD-1024", "Bàn 12", "17:45 - 29/09", "Chân Giò Muối Chiên Giòn x1, 7Up x2", "150.000 đ", "Đang Chờ"))
        adminOrderList.add(Order("#ORD-1025", "Bàn 05", "17:50 - 29/09", "Gà Nướng Mật Ong x1, Coca x1", "165.000 đ", "Đang Chế Biến"))
        adminOrderList.add(Order("#ORD-1023", "Bàn 02", "17:10 - 29/09", "Tôm Chiên Xù x1, Sting x2", "125.000 đ", "Hoàn Thành"))
    }

    private fun setupTabs() {
        binding.chipGroupAdminTabs.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            binding.layoutTabDashboard.visibility = View.GONE
            binding.layoutTabProducts.visibility = View.GONE
            binding.layoutTabOrders.visibility = View.GONE

            when (checkedIds[0]) {
                R.id.chipTabProducts -> binding.layoutTabProducts.visibility = View.VISIBLE
                R.id.chipTabOrders -> binding.layoutTabOrders.visibility = View.VISIBLE
                else -> binding.layoutTabDashboard.visibility = View.VISIBLE
            }
        }
    }

    private fun setupFoodRecyclerView() {
        adminFoodAdapter = AdminFoodAdapter(
            adminFoodList,
            onEditClick = { food -> showAddEditFoodDialog(food) },
            onDeleteClick = { food -> confirmDeleteFood(food) }
        )
        binding.rvAdminProducts.layoutManager = LinearLayoutManager(this)
        binding.rvAdminProducts.adapter = adminFoodAdapter
    }

    private fun setupOrderRecyclerView() {
        adminOrderAdapter = AdminOrderAdapter(adminOrderList) { order ->
            advanceOrderStatus(order)
        }
        binding.rvAdminOrders.layoutManager = LinearLayoutManager(this)
        binding.rvAdminOrders.adapter = adminOrderAdapter
    }

    private fun setupListeners() {
        binding.btnSwitchToUserApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnAddProduct.setOnClickListener {
            showAddEditFoodDialog(null)
        }
    }

    private fun showAddEditFoodDialog(existingFood: FoodItem?) {
        val dialogBinding = DialogAddEditFoodBinding.inflate(LayoutInflater.from(this))
        
        if (existingFood != null) {
            dialogBinding.tvDialogTitle.text = "✏️ Chỉnh Sửa Món Ăn"
            dialogBinding.etFoodName.setText(existingFood.name)
            dialogBinding.etFoodPrice.setText(existingFood.price.toString())
            dialogBinding.etFoodDesc.setText(existingFood.description)
            when (existingFood.category) {
                "Món phụ" -> dialogBinding.chipAddSide.isChecked = true
                "Nước uống" -> dialogBinding.chipAddDrink.isChecked = true
                else -> dialogBinding.chipAddMain.isChecked = true
            }
        } else {
            dialogBinding.tvDialogTitle.text = "➕ Thêm Món Ăn Mới"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSaveFood.setOnClickListener {
            val name = dialogBinding.etFoodName.text.toString().trim()
            val priceStr = dialogBinding.etFoodPrice.text.toString().trim()
            val desc = dialogBinding.etFoodDesc.text.toString().trim()

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên món và giá tiền", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toIntOrNull() ?: 0
            val priceFormatted = String.format(Locale.getDefault(), "%,d đ", price)

            val category = when {
                dialogBinding.chipAddSide.isChecked -> "Món phụ"
                dialogBinding.chipAddDrink.isChecked -> "Nước uống"
                else -> "Món chính"
            }

            if (existingFood != null) {
                // EDIT EXISTING FOOD
                val index = adminFoodList.indexOfFirst { it.id == existingFood.id }
                if (index != -1) {
                    adminFoodList[index] = FoodItem(
                        id = existingFood.id,
                        name = name,
                        category = category,
                        price = price,
                        priceFormatted = priceFormatted,
                        description = desc.ifEmpty { "$name thơm ngon chuẩn vị" }
                    )
                    adminFoodAdapter.updateList(adminFoodList)
                    Toast.makeText(this, "Đã cập nhật món '$name'!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // ADD NEW FOOD
                val newId = (adminFoodList.maxOfOrNull { it.id } ?: 0) + 1
                val newFood = FoodItem(
                    id = newId,
                    name = name,
                    category = category,
                    price = price,
                    priceFormatted = priceFormatted,
                    description = desc.ifEmpty { "$name thơm ngon chuẩn vị" }
                )
                adminFoodList.add(0, newFood)
                adminFoodAdapter.updateList(adminFoodList)
                binding.tvStatTotalProducts.text = adminFoodList.size.toString()
                Toast.makeText(this, "Đã thêm món mới '$name' thành công!", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmDeleteFood(food: FoodItem) {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Xác nhận xóa món")
            .setMessage("Bạn có chắc chắn muốn xóa món '${food.name}' khỏi thực đơn không?")
            .setPositiveButton("Xóa") { _, _ ->
                adminFoodList.remove(food)
                adminFoodAdapter.updateList(adminFoodList)
                binding.tvStatTotalProducts.text = adminFoodList.size.toString()
                Toast.makeText(this, "Đã xóa món '${food.name}'!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun advanceOrderStatus(order: Order) {
        val nextStatus = when (order.status) {
            "Đang Chờ" -> "Đang Chế Biến"
            "Đang Chế Biến" -> "Hoàn Thành"
            else -> "Hoàn Thành"
        }
        order.status = nextStatus
        adminOrderAdapter.updateList(adminOrderList)
        Toast.makeText(this, "Đơn ${order.id} đã chuyển sang trạng thái: $nextStatus", Toast.LENGTH_SHORT).show()
    }
}
