package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemFoodCardBinding
import com.example.chieuthu3_ketthucmonhoc.models.FoodItem

class FoodAdapter(
    private var foodList: List<FoodItem>,
    private val onAddToCartClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(val binding: ItemFoodCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        val context = holder.itemView.context

        holder.binding.tvFoodName.text = food.name
        holder.binding.tvCategoryBadge.text = food.category
        holder.binding.tvFoodDesc.text = food.description
        holder.binding.tvFoodPrice.text = food.priceFormatted

        // Set badge background based on category
        when (food.category) {
            "Món chính" -> {
                holder.binding.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_mon_chinh)
                holder.binding.tvCategoryBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_mon_chinh_text)
                )
            }
            "Món phụ" -> {
                holder.binding.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_mon_phu)
                holder.binding.tvCategoryBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_mon_phu_text)
                )
            }
            else -> {
                holder.binding.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_do_uong)
                holder.binding.tvCategoryBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.badge_do_uong_text)
                )
            }
        }

        holder.binding.btnAddToCart.setOnClickListener {
            onAddToCartClick(food)
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newList: List<FoodItem>) {
        foodList = newList
        notifyDataSetChanged()
    }
}
