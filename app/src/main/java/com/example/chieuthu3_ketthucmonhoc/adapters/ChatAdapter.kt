package com.example.chieuthu3_ketthucmonhoc.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chieuthu3_ketthucmonhoc.R
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemChatMessageBinding
import com.example.chieuthu3_ketthucmonhoc.models.ChatMessage

class ChatAdapter(
    private val messageList: List<ChatMessage>
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
        val msg = messageList[position]
        val context = holder.itemView.context

        holder.binding.tvChatMessageText.text = msg.text

        val params = holder.binding.tvChatMessageText.layoutParams as LinearLayout.LayoutParams

        if (msg.isUser) {
            holder.binding.chatBubbleContainer.gravity = Gravity.END
            params.gravity = Gravity.END
            holder.binding.tvChatMessageText.setBackgroundResource(R.drawable.bg_banner_red)
            holder.binding.tvChatMessageText.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
        } else {
            holder.binding.chatBubbleContainer.gravity = Gravity.START
            params.gravity = Gravity.START
            holder.binding.tvChatMessageText.setBackgroundResource(R.drawable.bg_header_button)
            holder.binding.tvChatMessageText.setTextColor(
                ContextCompat.getColor(context, R.color.black)
            )
        }
        holder.binding.tvChatMessageText.layoutParams = params
    }

    override fun getItemCount(): Int = messageList.size
}
