package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.R;
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemChatMessageBinding;
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemChatSuggestionBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    public interface OnSuggestedItemClickListener {
        void onAddSuggestedItem(MenuItem item);
    }

    private List<ChatMessage> chatList;
    private final OnSuggestedItemClickListener onSuggestedItemClickListener;

    public ChatAdapter(List<ChatMessage> chatList, OnSuggestedItemClickListener onSuggestedItemClickListener) {
        this.chatList = chatList != null ? chatList : new ArrayList<>();
        this.onSuggestedItemClickListener = onSuggestedItemClickListener;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        final ItemChatMessageBinding binding;

        public ChatViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvChatMessageText.setText(message.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.binding.tvChatMessageText.getLayoutParams();

        if (message.isUser()) {
            holder.binding.chatBubbleContainer.setGravity(Gravity.END);
            params.gravity = Gravity.END;
            holder.binding.tvChatMessageText.setBackgroundResource(R.drawable.bg_chat_user);
            holder.binding.tvChatMessageText.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.binding.layoutSuggestedItems.setVisibility(View.GONE);
        } else {
            holder.binding.chatBubbleContainer.setGravity(Gravity.START);
            params.gravity = Gravity.START;
            holder.binding.tvChatMessageText.setBackgroundResource(R.drawable.bg_chat_ai);
            holder.binding.tvChatMessageText.setTextColor(ContextCompat.getColor(context, R.color.black));

            // Render suggested items if any
            if (message.getSuggestedItems() != null && !message.getSuggestedItems().isEmpty()) {
                holder.binding.layoutSuggestedItems.setVisibility(View.VISIBLE);
                holder.binding.layoutSuggestedItems.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(context);
                for (MenuItem item : message.getSuggestedItems()) {
                    ItemChatSuggestionBinding itemBinding = ItemChatSuggestionBinding.inflate(
                            inflater, holder.binding.layoutSuggestedItems, false
                    );
                    itemBinding.imgSuggestFood.setImageResource(FoodImageResolver.resolveImage(item));
                    itemBinding.tvSuggestName.setText(item.getName());
                    itemBinding.tvSuggestPrice.setText(item.getFormattedPrice());
                    itemBinding.btnSuggestAdd.setOnClickListener(v -> {
                        if (onSuggestedItemClickListener != null) {
                            onSuggestedItemClickListener.onAddSuggestedItem(item);
                        }
                    });
                    holder.binding.layoutSuggestedItems.addView(itemBinding.getRoot());
                }
            } else {
                holder.binding.layoutSuggestedItems.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateList(List<ChatMessage> newList) {
        this.chatList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
