package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemChatMessageBinding
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemChatSuggestionBinding
import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

class ChatAdapter(
    private var chatList: List<ChatMessage>,
    private val onAddSuggestedItemClick: ((MenuItem) -> Unit)? = null
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatList[position]
        val context = holder.itemView.context

        holder.binding.tvChatMessageText.text = message.text

        val params = holder.binding.tvChatMessageText.layoutParams as LinearLayout.LayoutParams

        if (message.isUser) {
            holder.binding.chatBubbleContainer.gravity = Gravity.END
            params.gravity = Gravity.END
            holder.binding.tvChatMessageText.setBackgroundResource(R.drawable.bg_chat_user)
            holder.binding.tvChatMessageText.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.layoutSuggestedItems.visibility = View.GONE
        } else {
            holder.binding.chatBubbleContainer.gravity = Gravity.START
            params.gravity = Gravity.START
            holder.binding.tvChatMessageText.setBackgroundResource(R.drawable.bg_chat_ai)
            holder.binding.tvChatMessageText.setTextColor(ContextCompat.getColor(context, R.color.black))

            // Render suggested items if any
            if (message.suggestedItems.isNotEmpty()) {
                holder.binding.layoutSuggestedItems.visibility = View.VISIBLE
                holder.binding.layoutSuggestedItems.removeAllViews()
                val inflater = LayoutInflater.from(context)
                for (item in message.suggestedItems) {
                    val itemBinding = ItemChatSuggestionBinding.inflate(inflater, holder.binding.layoutSuggestedItems, false)
                    itemBinding.tvSuggestName.text = item.name
                    itemBinding.tvSuggestPrice.text = item.formattedPrice
                    itemBinding.btnSuggestAdd.setOnClickListener {
                        onAddSuggestedItemClick?.invoke(item)
                    }
                    holder.binding.layoutSuggestedItems.addView(itemBinding.root)
                }
            } else {
                holder.binding.layoutSuggestedItems.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

    fun updateList(newList: List<ChatMessage>) {
        this.chatList = newList
        notifyDataSetChanged()
    }
}
