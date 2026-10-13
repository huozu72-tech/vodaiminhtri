package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminFoodBinding
import com.example.chieuthu3_ketthucmonhoc.models.FoodItem

class AdminFoodAdapter(
    private var foodList: List<FoodItem>,
    private val onEditClick: (FoodItem) -> Unit,
    private val onDeleteClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<AdminFoodAdapter.AdminFoodViewHolder>() {

    class AdminFoodViewHolder(val binding: ItemAdminFoodBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFoodViewHolder {
        val binding = ItemAdminFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdminFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminFoodViewHolder, position: Int) {
        val food = foodList[position]
        val context = holder.itemView.context

        holder.binding.tvAdminFoodName.text = food.name
        holder.binding.tvAdminFoodCategory.text = food.category
        holder.binding.tvAdminFoodPrice.text = food.priceFormatted

        when (food.category) {
            "Món chính" -> {
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_mon_chinh)
                holder.binding.tvAdminFoodCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_mon_chinh_text)
                )
            }
            "Món phụ" -> {
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_mon_phu)
                holder.binding.tvAdminFoodCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_mon_phu_text)
                )
            }
            else -> {
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_do_uong)
                holder.binding.tvAdminFoodCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_do_uong_text)
                )
            }
        }

        holder.binding.btnEditFood.setOnClickListener { onEditClick(food) }
        holder.binding.btnDeleteFood.setOnClickListener { onDeleteClick(food) }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newList: List<FoodItem>) {
        foodList = newList
        notifyDataSetChanged()
    }
}
