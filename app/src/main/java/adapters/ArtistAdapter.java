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

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistaViewHolder> {

    private final List<Artistas> artistas;
    private final OnItemClickListener listener;

    /**
     * Interfaz para manejar los clics desde el Fragment o Activity.
     */
    public interface OnItemClickListener {
        void onItemClick(Artistas artista);
    }

    /**
     * Constructor del Adapter.
     */
    public ArtistAdapter(@NonNull List<Artistas> artistas, @NonNull OnItemClickListener listener) {
        this.artistas = artistas;
        this.listener = listener;
    }

    /**
     * ViewHolder: gestiona las vistas individuales y los clics.
     */
    public static class ArtistaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtista;
        TextView tvNombre;

        public ArtistaViewHolder(@NonNull View itemView, OnItemClickListener listener, List<Artistas> artistas) {
            super(itemView);
            ivArtista = itemView.findViewById(R.id.iv_artist_circle_image);
            tvNombre = itemView.findViewById(R.id.tv_artist_circle_name);

            // Configurar el click del ítem
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(artistas.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public ArtistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist_circle, parent, false);
        return new ArtistaViewHolder(view, listener, artistas);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistaViewHolder holder, int position) {
        Artistas artista = artistas.get(position);
        holder.tvNombre.setText(artista.getArtista());
        holder.ivArtista.setImageResource(artista.getImagenResourceId());
    }

    @Override
    public int getItemCount() {
        return artistas != null ? artistas.size() : 0;
    }
}
