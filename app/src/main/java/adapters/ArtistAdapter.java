package adapters;

import android.content.Context;
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
    private final OnItemClickListener listener; // Referencia a la interface

    // Interface para manejar los clics
    public interface OnItemClickListener {
        void onItemClick(Artistas artistas);
    }

    // Constructor del Adapter
    public ArtistAdapter(List<Artistas> artistas, OnItemClickListener listener) {
        this.artistas = artistas;
        this.listener = listener;
    }

    // --- A. ViewHolder: Almacén y Manejo de Clicks ---
    public class ArtistaViewHolder extends RecyclerView.ViewHolder {
        // Referencias a los Views de item_artist_circle.xml
        ImageView ivArtista;
        TextView tvNombre;

        public ArtistaViewHolder(@NonNull View itemView) {
            super(itemView);
            // 1. Obtener referencias de las vistas una sola vez
            ivArtista = itemView.findViewById(R.id.iv_artist_circle_image);
            tvNombre = itemView.findViewById(R.id.tv_artist_circle_name);

            // 2. Implementar el Click Listener en el ViewHolder
            itemView.setOnClickListener(v -> {
                // Obtener la posición del ítem actual
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // Notificar el click usando la Interface y pasar el objeto Artista
                    listener.onItemClick(artistas.get(position));
                }
            });
        }
    }

    // --- B. Método onCreateViewHolder() ---
    @NonNull
    @Override
    public ArtistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea (Infla) la plantilla XML del ítem (item_artist_circle.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist_circle, parent, false);
        return new ArtistaViewHolder(view);
    }

    // --- C. Método onBindViewHolder() ---
    @Override
    public void onBindViewHolder(@NonNull ArtistaViewHolder holder, int position) {
        // Obtiene el objeto Artista de la posición actual
        Artistas artistaActual = artistas.get(position);

        // Asigna los datos a los Views del ViewHolder
        holder.tvNombre.setText(artistaActual.getArtista());
        holder.ivArtista.setImageResource(artistaActual.getImagenResourceId());
    }

    // --- D. Método getItemCount() ---
    @Override
    public int getItemCount() {
        // Indica cuántos ítems existen en la lista
        return artistas.size();
    }
}