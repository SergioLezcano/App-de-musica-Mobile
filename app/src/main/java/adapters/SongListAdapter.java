package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appmusic_basico.R;

import java.util.List;

import models.Cancion_Reciente;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    private final List<Cancion_Reciente> canciones;
    private final OnSongClickListener listener;

    // Interfaz para manejar click
    public interface OnSongClickListener {
        void onSongClicked(Cancion_Reciente cancion);
    }

    public SongListAdapter(List<Cancion_Reciente> canciones, OnSongClickListener listener) {
        this.canciones = canciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_canciones, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Cancion_Reciente cancion = canciones.get(position);

        holder.tvTitulo.setText(cancion.getTitulo());
        holder.tvArtista.setText(cancion.getArtistaName());

        // Cargar imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(cancion.getCoverUrl())
                .placeholder(R.drawable.image_2930)
                .into(holder.ivImagen);

        // Click en toda la fila
        holder.itemView.setOnClickListener(v -> listener.onSongClicked(cancion));

        // Click en el botón "más opciones"
        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onSongClicked(cancion);
        });
    }

    @Override
    public int getItemCount() {
        return canciones.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImagen;
        TextView tvTitulo;
        TextView tvArtista;
        ImageButton btnMore;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImagen = itemView.findViewById(R.id.iv_album_artista);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_cancion);
            tvArtista = itemView.findViewById(R.id.tv_nombre_artista);
            btnMore = itemView.findViewById(R.id.iv_more_vertical);
        }
    }
}

