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
import models.Cancion_Reciente;

public class RecentlyPlayedAdapter extends RecyclerView.Adapter<RecentlyPlayedAdapter.ReproduccionRecienteViewHolder> {

    private final List<Cancion_Reciente> cancionesRecientes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cancion_Reciente cancionReciente);
    }

    public RecentlyPlayedAdapter(List<Cancion_Reciente> cancionesRecientes, OnItemClickListener listener) {
        this.cancionesRecientes = cancionesRecientes;
        this.listener = listener;
    }

    public class ReproduccionRecienteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtista;
        TextView tvNombre;
        TextView tvTituloCancion;

        public ReproduccionRecienteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtista = itemView.findViewById(R.id.img_recently_played_cover);
            tvNombre = itemView.findViewById(R.id.tv_recently_played_title);
            tvTituloCancion = itemView.findViewById(R.id.tv_recently_played_song);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(cancionesRecientes.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public ReproduccionRecienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recently_played, parent, false);
        return new ReproduccionRecienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReproduccionRecienteViewHolder holder, int position) {
        Cancion_Reciente cancion = cancionesRecientes.get(position);
        holder.tvTituloCancion.setText(cancion.getTitulo());
        holder.tvNombre.setText(cancion.getArtistaName());

        if (cancion.isFromSpotify() && cancion.getCoverUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(cancion.getCoverUrl())
                    .placeholder(R.drawable.image_2930)
                    .into(holder.ivArtista);
        } else {
            // Si no hay URL, usa un placeholder
            holder.ivArtista.setImageResource(R.drawable.image_2930);
        }

    }

    @Override
    public int getItemCount() {
        return cancionesRecientes.size();
    }
}
