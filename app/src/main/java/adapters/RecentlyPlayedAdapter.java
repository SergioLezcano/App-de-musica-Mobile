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
import models.Cancion_Reciente;

public class RecentlyPlayedAdapter extends RecyclerView.Adapter<RecentlyPlayedAdapter.ReproduccionRecienteViewHolder>{

    private final List<Cancion_Reciente> canciones_recientes;

    private final RecentlyPlayedAdapter.OnItemClickListener listener; // Referencia a la interface

    // Interface para manejar los clics
    public interface OnItemClickListener {
        void onItemClick(Cancion_Reciente cancionReciente);
    }

    public RecentlyPlayedAdapter(List<Cancion_Reciente> canciones_recientes, OnItemClickListener listener){
        this.canciones_recientes = canciones_recientes;
        this.listener = listener;
    }

    // --- A. ViewHolder: Almacén y Manejo de Clicks ---
    public class ReproduccionRecienteViewHolder extends RecyclerView.ViewHolder {

        ImageView ivArtista;
        TextView tvNombre;
        TextView tvTitulo_Cancion;

        public ReproduccionRecienteViewHolder(@NonNull View itemView){
            super(itemView);

            // 1. Obtener referencias de las vistas una sola vez
            ivArtista = itemView.findViewById(R.id.img_recently_played_cover);
            tvNombre = itemView.findViewById(R.id.tv_recently_played_title);
            tvTitulo_Cancion = itemView.findViewById(R.id.tv_recently_played_song);

            itemView.setOnClickListener(v -> {
                // Obtener la posición del ítem actual
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // Notificar el click usando la Interface y pasar el objeto Artista
                    listener.onItemClick(canciones_recientes.get(position));
                }
            });
        }

    }

    // --- B. Método onCreateViewHolder() ---
    @NonNull
    @Override
    public ReproduccionRecienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea (Infla) la plantilla XML del ítem (item_recently_played.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recently_played, parent, false);
        return new ReproduccionRecienteViewHolder(view);
    }

    // --- C. Método onBindViewHolder() ---
    @Override
    public void onBindViewHolder(@NonNull RecentlyPlayedAdapter.ReproduccionRecienteViewHolder holder, int position) {
        // Obtiene el objeto Canciones de la posición actual
        Cancion_Reciente cancionActual = canciones_recientes.get(position);

        // Asigna los datos a los Views del ViewHolder
        holder.ivArtista.setImageResource(cancionActual.getResourceId());
        holder.tvNombre.setText(cancionActual.getArtistaName());
        holder.tvTitulo_Cancion.setText(cancionActual.getTitulo());

    }

    // --- D. Método getItemCount() ---
    @Override
    public int getItemCount() {
        // Indica cuántos ítems existen en la lista
        return canciones_recientes.size();
    }
}
