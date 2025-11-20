package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appmusic_basico.R;

import java.util.List;

import models.SearchResultItem;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {

    private final List<SearchResultItem> resultsList;
    private final OnItemClickListener listener;

    // Interfaz para manejar el clic en cualquier resultado
    public interface OnItemClickListener {
        void onItemClick(SearchResultItem item);
    }

    public SearchResultAdapter(List<SearchResultItem> resultsList, OnItemClickListener listener) {
        this.resultsList = resultsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        SearchResultItem item = resultsList.get(position);

        // 1. Título y Subtítulo
        holder.tvTitle.setText(item.getTitle());

        // El subtítulo puede ser el nombre del artista, o el tipo de ítem
        String subtitleText = item.getSubtitle();

        // Añadir el tipo al subtítulo si el ítem es un artista o álbum
        if ("artist".equals(item.getType())) {
            subtitleText = "Artista";
        } else if ("album".equals(item.getType())) {
            // Si el subtítulo es el artista, mostramos "Álbum · [Nombre del Artista]"
            subtitleText = "Álbum · " + item.getSubtitle();
        } else if ("track".equals(item.getType())) {
            // Si el subtítulo es el artista, mostramos "Canción · [Nombre del Artista]"
            subtitleText = "Canción · " + item.getSubtitle();
        }

        holder.tvSubtitle.setText(subtitleText);


        // 2. Imagen (usando Glide)
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                // Usamos un placeholder genérico
                .placeholder(R.drawable.album_art_placeholder)
                // Usamos crop para artistas/álbumes y círculo para canciones (o viceversa)
                .centerCrop()
                .error(R.drawable.album_art_placeholder)
                .into(holder.ivImage);

        // 3. Listener (Click)
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    // --- ViewHolder ---
    static class ResultViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvTitle;
        TextView tvSubtitle;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.iv_search_image);
            tvTitle = itemView.findViewById(R.id.tv_search_title);
            tvSubtitle = itemView.findViewById(R.id.tv_search_subtitle);
        }
    }
}
