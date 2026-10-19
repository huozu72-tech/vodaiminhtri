package com.example.chieuthu3_ketthucmonhoc.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminOrderBinding
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus

class AdminOrderAdapter(
    private var orderList: List<Order>,
    private val onChangeStatusClick: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder>() {

    class AdminOrderViewHolder(val binding: ItemAdminOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdminOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = orderList[position]
        val context = holder.itemView.context

        holder.binding.tvOrderId.text = "${order.orderId} • ${order.tableNumber}"
        holder.binding.tvOrderTime.text = "Thời gian: ${order.formattedTime}"
        holder.binding.tvOrderItemsSummary.text = "Món: ${order.itemsSummary}"
        holder.binding.tvOrderTotalPrice.text = order.formattedTotal
        holder.binding.tvOrderStatusBadge.text = order.status.displayName

        try {
            holder.binding.tvOrderStatusBadge.setTextColor(Color.parseColor(order.status.colorHex))
        } catch (_: Exception) {
            holder.binding.tvOrderStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_status_pending))
        }

        holder.binding.btnChangeStatus.setOnClickListener { onChangeStatusClick(order) }
    }

    override fun getItemCount(): Int = orderList.size

    fun updateList(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }
}
