package adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.appmusic_basico.R;
import java.util.List;
import models.ArtistExplorerItem;

public class ArtistExplorerAdapter extends RecyclerView.Adapter<ArtistExplorerAdapter.ArtistViewHolder> {

    private final List<ArtistExplorerItem> artistList;
    private final OnArtistClickListener listener;

    public interface OnArtistClickListener {
        void onArtistClick(ArtistExplorerItem artist);
    }

    public ArtistExplorerAdapter(List<ArtistExplorerItem> artistList, OnArtistClickListener listener) {
        this.artistList = artistList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_all, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        ArtistExplorerItem artist = artistList.get(position);

        holder.tvArtistName.setText(artist.getArtistName());

        // 🆕 Aplicar Color de Fondo al CardView
        try {
            int color = Color.parseColor(artist.getBackgroundColorHex());
            // ¡CAMBIO AQUÍ! Aplicar el color a la tarjeta principal
            holder.cardCategoryItem.setCardBackgroundColor(color);
        } catch (IllegalArgumentException e) {
            // Si el código HEX no es válido, usa un color predeterminado
            holder.cardCategoryItem.setCardBackgroundColor(Color.parseColor("#343434"));
        }

//        Glide.with(holder.itemView.getContext())
//                .load(artist.getImageUrl())
//                .placeholder(R.drawable.album_art_placeholder)
//                // 🛑 CRÍTICO: Transformación a circular para artistas
//                .transform(new CircleCrop())
//                .error(R.drawable.album_art_placeholder)
//                .into(holder.ivArtistImage);

        holder.itemView.setOnClickListener(v -> listener.onArtistClick(artist));
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    // --- ViewHolder ---
    static class ArtistViewHolder extends RecyclerView.ViewHolder {

        androidx.cardview.widget.CardView cardCategoryItem;
        TextView tvArtistName;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategoryItem = itemView.findViewById(R.id.card_category_item);
            tvArtistName = itemView.findViewById(R.id.tv_title_target);
        }
    }
}
