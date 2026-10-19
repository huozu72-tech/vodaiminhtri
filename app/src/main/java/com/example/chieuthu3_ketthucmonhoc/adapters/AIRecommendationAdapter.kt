package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAiRecommendationCardBinding
import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

class AIRecommendationAdapter(
    private var recommendationList: List<AIRecommendation>,
    private val onAddToCartClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<AIRecommendationAdapter.RecViewHolder>() {

    inner class RecViewHolder(val binding: ItemAiRecommendationCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val binding = ItemAiRecommendationCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        val item = recommendationList[position]

        holder.binding.tvAiReasonBadge.text = item.reason
        holder.binding.tvAiDishName.text = item.menuItem.name
        holder.binding.tvAiDishPrice.text = item.menuItem.formattedPrice

        holder.binding.btnAiAddToCart.setOnClickListener {
            onAddToCartClick(item.menuItem)
        }
    }

    override fun getItemCount(): Int = recommendationList.size

    fun updateList(newList: List<AIRecommendation>) {
        this.recommendationList = newList
        notifyDataSetChanged()
    }
}
