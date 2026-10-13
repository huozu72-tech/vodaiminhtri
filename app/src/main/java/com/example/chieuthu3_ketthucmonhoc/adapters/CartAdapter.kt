package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemCartEntryBinding
import com.example.chieuthu3_ketthucmonhoc.models.CartItem
import java.util.Locale

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val onItemQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        holder.binding.tvCartItemName.text = item.foodItem.name
        val totalItemPrice = item.foodItem.price * item.quantity
        holder.binding.tvCartItemPrice.text = String.format(Locale.getDefault(), "%,d đ", totalItemPrice)
        holder.binding.tvQuantity.text = item.quantity.toString()

        holder.binding.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onItemQuantityChanged()
        }

        holder.binding.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
            } else {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    cartList.removeAt(currentPos)
                    notifyItemRemoved(currentPos)
                }
            }
            onItemQuantityChanged()
        }
    }

    override fun getItemCount(): Int = cartList.size
}
