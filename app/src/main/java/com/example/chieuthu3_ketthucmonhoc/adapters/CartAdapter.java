package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.databinding.ItemCartEntryBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onQuantityPlus(CartItem item);
        void onQuantityMinus(CartItem item);
        default void onRemoveItem(CartItem item) {}
        default void onEditNote(CartItem item) {}
    }

    private List<CartItem> cartList;
    private final OnCartItemActionListener actionListener;

    public CartAdapter(List<CartItem> cartList, OnCartItemActionListener actionListener) {
        this.cartList = cartList != null ? cartList : new ArrayList<>();
        this.actionListener = actionListener;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        final ItemCartEntryBinding binding;

        public CartViewHolder(ItemCartEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartEntryBinding binding = ItemCartEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.binding.imgCartFood.setImageResource(FoodImageResolver.resolveImage(item.getMenuItem()));
        holder.binding.tvCartItemName.setText(item.getMenuItem().getName());
        holder.binding.tvCartItemPrice.setText(
                item.getMenuItem().getFormattedPrice() + " × " + item.getQuantity() + " = " + item.getFormattedSubtotal()
        );
        holder.binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

        String note = item.getNote();
        if (note != null && !note.trim().isEmpty()) {
            holder.binding.tvCartItemNote.setText("📝 Ghi chú bếp: " + note);
        } else {
            holder.binding.tvCartItemNote.setText("📝 + Ghi chú bếp (Ít cay, không hành...)");
        }

        holder.binding.tvCartItemNote.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditNote(item);
            }
        });

        holder.binding.btnPlus.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onQuantityPlus(item);
            }
        });

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onQuantityMinus(item);
            }
        });

        holder.binding.btnRemoveCartItem.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRemoveItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public void updateList(List<CartItem> newList) {
        this.cartList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
