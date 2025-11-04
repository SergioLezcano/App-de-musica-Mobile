package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.R;

import java.util.List;

import models.Artistas;
import com.bumptech.glide.Glide;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private final List<Artistas> artistList;

    public ArtistAdapter(List<Artistas> artistList) {
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_circle, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artistas artista = artistList.get(position);

        holder.tvArtistName.setText(artista.getNombre());

        // 🚀 LÓGICA DE CARGA DE IMAGEN CON GLIDE
        if (artista.getImagenUrl() != null && !artista.getImagenUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(artista.getImagenUrl())
                    .circleCrop() // Opcional: si quieres que las imágenes de artista sean circulares
                    .placeholder(R.drawable.image_1034) // Placeholder mientras carga
                    .error(R.drawable.image_2930)       // Placeholder si falla la carga
                    .into(holder.ivArtista);
        } else {
            // Si no hay URL (es null o está vacío), usa solo el placeholder
            holder.ivArtista.setImageResource(R.drawable.image_1034);
        }
        holder.tvArtistName.setText(artista.getNombre());
        // Opcional: Agregar un OnClickListener para reproducir música del artista
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView tvArtistName;
        ImageView ivArtista;
        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArtistName = itemView.findViewById(R.id.tv_artist_circle_name);
            ivArtista = itemView.findViewById(R.id.iv_artist_circle_image);
        }
    }
}