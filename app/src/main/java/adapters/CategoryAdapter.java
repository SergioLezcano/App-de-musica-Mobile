package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import android.graphics.Color;
import com.example.appmusic_basico.R;

import java.util.List;

import models.CategoryItem;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<CategoryItem> categoryList;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem category);
    }

    public CategoryAdapter(List<CategoryItem> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_all, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());

        // 🆕 Aplicar Color de Fondo
        try {
            int color = Color.parseColor(category.getBackgroundColorHex());
            holder.itemView.setBackgroundColor(color);
        } catch (IllegalArgumentException e) {
            // Si el código HEX no es válido, usa un color predeterminado
            holder.itemView.setBackgroundColor(Color.parseColor("#343434"));
        }

        Glide.with(holder.itemView.getContext())
                .load(category.getImageUrl())
                .placeholder(R.drawable.album_art_placeholder)
                .centerCrop()
                .error(R.drawable.album_art_placeholder)
                .into(holder.ivCategoryImage);

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // --- ViewHolder ---
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCategoryImage;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryImage = itemView.findViewById(R.id.iv_image_explored);
            tvCategoryName = itemView.findViewById(R.id.tv_title_target);
        }
    }
}