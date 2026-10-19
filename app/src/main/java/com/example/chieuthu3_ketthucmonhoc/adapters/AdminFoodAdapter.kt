package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminFoodBinding
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

class AdminFoodAdapter(
    private var foodList: List<MenuItem>,
    private val onEditClick: (MenuItem) -> Unit,
    private val onDeleteClick: (MenuItem) -> Unit
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
        holder.binding.tvAdminFoodCategory.text = food.category.displayName
        holder.binding.tvAdminFoodPrice.text = food.formattedPrice

        when (food.category) {
            FoodCategory.MAIN_DISH -> {
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_mon_chinh)
                holder.binding.tvAdminFoodCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_mon_chinh_text)
                )
            }
            FoodCategory.SIDE_DISH -> {
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_mon_phu)
                holder.binding.tvAdminFoodCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_mon_phu_text)
                )
            }
            FoodCategory.BEVERAGE -> {
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

    fun updateList(newList: List<MenuItem>) {
        foodList = newList
        notifyDataSetChanged()
    }
}
