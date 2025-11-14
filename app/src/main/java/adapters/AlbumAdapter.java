package adapters;

import android.content.Context;
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

import models.AlbumFavorito;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final List<AlbumFavorito> albumList;
    private final AlbumClickListener clickListener;

    public interface AlbumClickListener {
        void onAlbumClick(AlbumFavorito album);
    }

    public AlbumAdapter(List<AlbumFavorito> albumList, AlbumClickListener clickListener) {
        this.albumList = albumList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently_played, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        AlbumFavorito album = albumList.get(position);
        Context context = holder.itemView.getContext();

        // 💡 Mapeo de datos a los Views:
        holder.tvAlbumName.setText(album.getAlbumName());
        holder.tvArtistName.setText(album.getArtistName());

        Glide.with(context)
                .load(album.getImageUrl())
                .placeholder(R.drawable.image_2930)
                .error(R.drawable.image_2930)
                .into(holder.ivAlbumArt);

        holder.itemView.setOnClickListener(v -> clickListener.onAlbumClick(album));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAlbumArt;
        TextView tvAlbumName; // Mapeado a tv_recently_played_song (título de la pista)
        TextView tvArtistName; // Mapeado a tv_recently_played_title (nombre del artista)

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            // Usamos los IDs del layout item_recently_played.
            ivAlbumArt = itemView.findViewById(R.id.img_recently_played_cover);
            tvArtistName = itemView.findViewById(R.id.tv_recently_played_title);
            tvAlbumName = itemView.findViewById(R.id.tv_recently_played_song);
        }
    }
}