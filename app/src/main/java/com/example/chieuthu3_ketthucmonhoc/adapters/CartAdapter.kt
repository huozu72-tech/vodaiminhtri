package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemCartEntryBinding
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem

class CartAdapter(
    private var cartList: List<CartItem>,
    private val onQuantityPlus: (CartItem) -> Unit,
    private val onQuantityMinus: (CartItem) -> Unit
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

        holder.binding.tvCartItemName.text = item.menuItem.name
        holder.binding.tvCartItemPrice.text = item.formattedSubtotal
        holder.binding.tvQuantity.text = item.quantity.toString()

        holder.binding.btnPlus.setOnClickListener {
            onQuantityPlus(item)
        }

        holder.binding.btnMinus.setOnClickListener {
            onQuantityMinus(item)
        }
    }

    override fun getItemCount(): Int = cartList.size

    fun updateList(newList: List<CartItem>) {
        this.cartList = newList
        notifyDataSetChanged()
    }
}
