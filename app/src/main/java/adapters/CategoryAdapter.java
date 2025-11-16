package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import com.example.appmusic_basico.R;
import androidx.cardview.widget.CardView;

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

        // 🆕 Aplicar Color de Fondo al CardView
        try {
            int color = Color.parseColor(category.getBackgroundColorHex());
            holder.cardCategoryItem.setCardBackgroundColor(color);
        } catch (IllegalArgumentException e) {
            // Si el código HEX no es válido, usa un color predeterminado
            holder.cardCategoryItem.setCardBackgroundColor(Color.parseColor("#343434"));
        }

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // --- ViewHolder ---
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        CardView cardCategoryItem;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategoryItem = itemView.findViewById(R.id.card_category_item);
            tvCategoryName = itemView.findViewById(R.id.tv_title_target);
        }
    }
}