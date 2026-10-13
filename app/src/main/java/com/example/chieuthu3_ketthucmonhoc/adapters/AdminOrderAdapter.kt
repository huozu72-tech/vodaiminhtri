package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminOrderBinding
import com.example.chieuthu3_ketthucmonhoc.models.Order

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

        holder.binding.tvOrderId.text = "${order.id} • ${order.tableName}"
        holder.binding.tvOrderTime.text = "Thời gian: ${order.timeString}"
        holder.binding.tvOrderItemsSummary.text = "Món: ${order.itemsSummary}"
        holder.binding.tvOrderTotalPrice.text = order.totalPriceFormatted
        holder.binding.tvOrderStatusBadge.text = order.status

        if (order.status == "Hoàn Thành") {
            holder.binding.tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_mon_chinh)
            holder.binding.tvOrderStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.admin_status_complete)
            )
        } else {
            holder.binding.tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_badge_mon_phu)
            holder.binding.tvOrderStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.admin_status_pending)
            )
        }

        holder.binding.btnChangeStatus.setOnClickListener { onChangeStatusClick(order) }
    }

    override fun getItemCount(): Int = orderList.size

    fun updateList(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }
}
