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
import models.AlbumExplorerItem;
import android.graphics.Color;

public class AlbumExplorerAdapter extends RecyclerView.Adapter<AlbumExplorerAdapter.AlbumViewHolder> {

    private final List<AlbumExplorerItem> albumList;
    private final OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(AlbumExplorerItem album);
    }

    public AlbumExplorerAdapter(List<AlbumExplorerItem> albumList, OnAlbumClickListener listener) {
        this.albumList = albumList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reutilizamos el layout de la tarjeta de exploración
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_all, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        AlbumExplorerItem album = albumList.get(position);

        // Mostramos el nombre del álbum y el artista
        holder.tvAlbumInfo.setText(album.getAlbumName() + "\n" + album.getArtistName());

        // 🆕 Aplicar Color de Fondo al CardView
        try {
            int color = Color.parseColor(album.getBackgroundColorHex());
            // ¡CAMBIO AQUÍ! Aplicar el color a la tarjeta principal
            holder.cardCategoryItem.setCardBackgroundColor(color);
        } catch (IllegalArgumentException e) {
            // Si el código HEX no es válido, usa un color predeterminado
            holder.cardCategoryItem.setCardBackgroundColor(Color.parseColor("#343434"));
        }

//        Glide.with(holder.itemView.getContext())
//                .load(album.getImageUrl())
//                .placeholder(R.drawable.album_art_placeholder)
//                .centerCrop()
//                .error(R.drawable.album_art_placeholder)
//                .into(holder.ivAlbumImage);

        holder.itemView.setOnClickListener(v -> listener.onAlbumClick(album));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    // --- ViewHolder ---
    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cardCategoryItem;
        TextView tvAlbumInfo;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            cardCategoryItem = itemView.findViewById(R.id.card_category_item);
            tvAlbumInfo = itemView.findViewById(R.id.tv_title_target);

            // 💡 Opcional: Ajustar el tamaño del texto para que quepan dos líneas
            tvAlbumInfo.setTextSize(12f);
            tvAlbumInfo.setMaxLines(2);
        }
    }
}