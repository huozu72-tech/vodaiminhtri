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
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adminFoodAdapter: AdminFoodAdapter
    private lateinit var adminOrderAdapter: AdminOrderAdapter

    // Services via AppServiceLocator (Clean OOP Architecture)
    private val menuService = AppServiceLocator.menuService
    private val orderService = AppServiceLocator.orderService
    private val dashboardService = AppServiceLocator.dashboardService
    private val authService = AppServiceLocator.authService

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

        setupTabs()
        setupFoodRecyclerView()
        setupOrderRecyclerView()
        setupListeners()
        refreshDashboardMetrics()
    }

    override fun onResume() {
        super.onResume()
        refreshDashboardMetrics()
        adminFoodAdapter.updateList(menuService.getAllMenuItems())
        adminOrderAdapter.updateList(orderService.getAllOrders())
    }

    private fun setupTabs() {
        binding.chipGroupAdminTabs.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            binding.layoutTabDashboard.visibility = View.GONE
            binding.layoutTabProducts.visibility = View.GONE
            binding.layoutTabOrders.visibility = View.GONE

            when (checkedIds[0]) {
                R.id.chipTabProducts -> {
                    binding.layoutTabProducts.visibility = View.VISIBLE
                    adminFoodAdapter.updateList(menuService.getAllMenuItems())
                }
                R.id.chipTabOrders -> {
                    binding.layoutTabOrders.visibility = View.VISIBLE
                    adminOrderAdapter.updateList(orderService.getAllOrders())
                }
                else -> {
                    binding.layoutTabDashboard.visibility = View.VISIBLE
                    refreshDashboardMetrics()
                }
            }
        }
    }

    private fun refreshDashboardMetrics() {
        val metrics = dashboardService.getDashboardMetrics()
        binding.tvStatTotalOrders.text = metrics.totalOrdersCount.toString()
        binding.tvStatPendingOrders.text = metrics.pendingOrdersCount.toString()
        binding.tvStatTotalProducts.text = metrics.totalMenuItemsCount.toString()
        binding.tvStatTotalRevenue.text = metrics.formattedRevenue
        binding.tvStatCompletionRate.text = "Tỷ Lệ Hoàn Thành Đơn: ${metrics.completionRatePercent}% 🎯"

        val currentAdmin = authService.getCurrentAdmin()
        if (currentAdmin != null) {
            binding.tvAdminGreeting.text = currentAdmin.fullName
        } else {
            binding.tvAdminGreeting.text = "Quản Trị Viên"
        }
    }

    private fun setupFoodRecyclerView() {
        adminFoodAdapter = AdminFoodAdapter(
            foodList = menuService.getAllMenuItems(),
            onEditClick = { menuItem -> showAddEditFoodDialog(menuItem) },
            onDeleteClick = { menuItem -> confirmDeleteFood(menuItem) }
        )
        binding.rvAdminProducts.layoutManager = LinearLayoutManager(this)
        binding.rvAdminProducts.adapter = adminFoodAdapter
    }

    private fun setupOrderRecyclerView() {
        adminOrderAdapter = AdminOrderAdapter(
            orderList = orderService.getAllOrders(),
            onChangeStatusClick = { order -> showChangeOrderStatusDialog(order) }
        )
        binding.rvAdminOrders.layoutManager = LinearLayoutManager(this)
        binding.rvAdminOrders.adapter = adminOrderAdapter
    }

    private fun setupListeners() {
        binding.btnSwitchToUserApp.setOnClickListener {
            finish()
        }

        binding.btnAddProduct.setOnClickListener {
            showAddEditFoodDialog(null)
        }
    }

    private fun showAddEditFoodDialog(food: MenuItem?) {
        val dialogBinding = DialogAddEditFoodBinding.inflate(LayoutInflater.from(this))
        val isEditing = food != null

        if (isEditing) {
            dialogBinding.tvDialogTitle.text = "📝 Chỉnh Sửa Món Ăn"
            dialogBinding.etFoodName.setText(food!!.name)
            dialogBinding.etFoodPrice.setText(food.price.toString())
            dialogBinding.etFoodDesc.setText(food.description)

            when (food.category) {
                FoodCategory.MAIN_DISH -> dialogBinding.chipAddMain.isChecked = true
                FoodCategory.SIDE_DISH -> dialogBinding.chipAddSide.isChecked = true
                FoodCategory.BEVERAGE -> dialogBinding.chipAddDrink.isChecked = true
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
                Toast.makeText(this, "Vui lòng nhập tên món và giá bán!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toLongOrNull() ?: 0L
            if (price <= 0) {
                Toast.makeText(this, "Giá bán không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = when (dialogBinding.chipGroupAddCategory.checkedChipId) {
                R.id.chipAddSide -> FoodCategory.SIDE_DISH
                R.id.chipAddDrink -> FoodCategory.BEVERAGE
                else -> FoodCategory.MAIN_DISH
            }

            if (isEditing) {
                val updated = food!!.copy(
                    name = name,
                    price = price,
                    description = desc,
                    category = category
                )
                menuService.updateMenuItem(updated)
                Toast.makeText(this, "Đã cập nhật món '${updated.name}'!", Toast.LENGTH_SHORT).show()
            } else {
                menuService.createMenuItem(
                    name = name,
                    description = desc,
                    price = price,
                    category = category,
                    tasteAttributes = listOf(TasteAttribute.SAVORY)
                )
                Toast.makeText(this, "Đã thêm món mới thành công!", Toast.LENGTH_SHORT).show()
            }

            adminFoodAdapter.updateList(menuService.getAllMenuItems())
            refreshDashboardMetrics()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmDeleteFood(food: MenuItem) {
        AlertDialog.Builder(this)
            .setTitle("Xác Nhận Xóa")
            .setMessage("Bạn có chắc chắn muốn xóa món '${food.name}' khỏi thực đơn?")
            .setPositiveButton("Xóa") { _, _ ->
                menuService.deleteMenuItem(food.id)
                adminFoodAdapter.updateList(menuService.getAllMenuItems())
                refreshDashboardMetrics()
                Toast.makeText(this, "Đã xóa món '${food.name}'!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showChangeOrderStatusDialog(order: Order) {
        val statusList = arrayOf(
            OrderStatus.PENDING.displayName,
            OrderStatus.PREPARING.displayName,
            OrderStatus.COMPLETED.displayName,
            OrderStatus.CANCELLED.displayName
        )

        AlertDialog.Builder(this)
            .setTitle("Cập Nhật Trạng Thái Đơn ${order.orderId}")
            .setItems(statusList) { _, which ->
                val newStatus = when (which) {
                    1 -> OrderStatus.PREPARING
                    2 -> OrderStatus.COMPLETED
                    3 -> OrderStatus.CANCELLED
                    else -> OrderStatus.PENDING
                }

                orderService.updateOrderStatus(order.orderId, newStatus)
                adminOrderAdapter.updateList(orderService.getAllOrders())
                refreshDashboardMetrics()
                Toast.makeText(this, "Đã chuyển đơn ${order.orderId} sang '${newStatus.displayName}'", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }
}
