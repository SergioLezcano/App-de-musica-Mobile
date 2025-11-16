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

import models.PlaylistItem;

public class PlaylistGridAdapter extends RecyclerView.Adapter<PlaylistGridAdapter.SongAlbumViewHolder> {

    private final List<PlaylistItem> songList;
    private final OnSongClickListener listener;

    // --- Interfaz de Click Listener ---
    public interface OnSongClickListener {
        void onSongClick(PlaylistItem song);
    }

    // --- Constructor ---
    public PlaylistGridAdapter(List<PlaylistItem> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    // -------------------------------------------------------------------
    // Métodos principales del RecyclerView.Adapter
    // -------------------------------------------------------------------

    @NonNull
    @Override
    public SongAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid_card, parent, false);
        return new SongAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAlbumViewHolder holder, int position) {
        // Obtener el objeto de datos
        PlaylistItem song = songList.get(position);

        // 1. Establecer el texto usando los getters de SongItem
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        // 2. Cargar la imagen usando Glide
        Glide.with(holder.itemView.getContext())
                .load(song.getCoverUrl())
                .centerCrop()
                .placeholder(R.drawable.image_1034)
                .error(R.drawable.album_art_placeholder)
                .into(holder.ivCover);

        // 3. Implementar el Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    // -------------------------------------------------------------------
    /**
     * ViewHolder: Contiene y gestiona las vistas de cada elemento de la lista.
     */
    public static class SongAlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvTitle;
        TextView tvArtist;

        public SongAlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            // Asignar IDs del XML item_grid_card
            ivCover = itemView.findViewById(R.id.img_grid_cover);
            tvTitle = itemView.findViewById(R.id.tv_grid_title);
            tvArtist = itemView.findViewById(R.id.tv_grid_subtitle);
        }
    }
}
