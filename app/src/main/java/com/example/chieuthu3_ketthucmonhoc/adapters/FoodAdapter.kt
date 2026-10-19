package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemFoodCardBinding
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

class FoodAdapter(
    private var foodList: List<MenuItem>,
    private val onAddToCartClick: (MenuItem) -> Unit
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
        val item = foodList[position]

        holder.binding.tvFoodName.text = item.name
        holder.binding.tvFoodDesc.text = item.description
        holder.binding.tvFoodPrice.text = item.formattedPrice
        holder.binding.tvCategoryBadge.text = item.category.displayName

        val context = holder.itemView.context
        when (item.category) {
            FoodCategory.MAIN_DISH -> {
                holder.binding.tvCategoryBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_mon_chinh)
                holder.binding.tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_mon_chinh_text))
            }
            FoodCategory.SIDE_DISH -> {
                holder.binding.tvCategoryBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_mon_phu)
                holder.binding.tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_mon_phu_text))
            }
            FoodCategory.BEVERAGE -> {
                holder.binding.tvCategoryBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_do_uong)
                holder.binding.tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_do_uong_text))
            }
        }

        holder.binding.btnAddToCart.setOnClickListener {
            onAddToCartClick(item)
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newList: List<MenuItem>) {
        this.foodList = newList
        notifyDataSetChanged()
    }
}
