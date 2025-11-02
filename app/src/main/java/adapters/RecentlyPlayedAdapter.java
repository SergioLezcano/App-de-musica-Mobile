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

    private final List<Cancion_Reciente> canciones_recientes;
    private final OnItemClickListener listener;

    // Interface para manejar clics
    public interface OnItemClickListener {
        void onItemClick(Cancion_Reciente cancionReciente);
    }

    public RecentlyPlayedAdapter(List<Cancion_Reciente> canciones_recientes, OnItemClickListener listener) {
        this.canciones_recientes = canciones_recientes;
        this.listener = listener;
    }

    public class ReproduccionRecienteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtista;
        TextView tvNombre;
        TextView tvTitulo_Cancion;

        public ReproduccionRecienteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtista = itemView.findViewById(R.id.img_recently_played_cover);
            tvNombre = itemView.findViewById(R.id.tv_recently_played_title);
            tvTitulo_Cancion = itemView.findViewById(R.id.tv_recently_played_song);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(canciones_recientes.get(position));
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
        Cancion_Reciente cancion = canciones_recientes.get(position);

        holder.tvTitulo_Cancion.setText(cancion.getTitulo());
        holder.tvNombre.setText(cancion.getArtistaName());

        if (cancion.isFromSpotify() && cancion.getCoverUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(cancion.getCoverUrl())
                    .placeholder(R.drawable.image_2930)
                    .into(holder.ivArtista);
        } else {
            holder.ivArtista.setImageResource(cancion.getCoverResourceId());
        }
    }

    @Override
    public int getItemCount() {
        return canciones_recientes.size();
    }
}
