package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView; // Ejemplo: Si item_album_detalle usa TextView
import android.widget.ImageView; // Ejemplo: Si item_album_detalle usa ImageView

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmusic_basico.R;

import java.util.List;

import models.Cancion_Reciente;

public class AlbumDetalleAdapter extends RecyclerView.Adapter<AlbumDetalleAdapter.AlbumViewHolder> {
    private final List<Cancion_Reciente> albumList;
    private final AlbumClickListener clickListener;

    public interface AlbumClickListener {
        void onTrackClick(Cancion_Reciente track);
        void onMoreOptionsClick(Cancion_Reciente track, View view);
    }

    public AlbumDetalleAdapter(List<Cancion_Reciente> albumList, AlbumClickListener clickListener) {
        this.albumList = albumList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_detalle, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        final Cancion_Reciente track = albumList.get(position);

        // 🆕 Lógica de vinculación
        //holder.tvTrackNumber.setText(String.valueOf(position + 1));
        holder.tvTrackTitle.setText(track.getTitulo());
        holder.tvArtistName.setText(track.getArtistaName());

        // 🆕 Manejo del clic en la canción
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTrackClick(track);
            }
        });

        // 🆕 Listener para el botón de Opciones
        holder.ibMoreOptions.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMoreOptionsClick(track, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageButton ibMoreOptions;
        TextView tvTrackTitle;
        TextView tvArtistName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            ibMoreOptions = itemView.findViewById(R.id.iv_more_vertical_album);
            tvTrackTitle = itemView.findViewById(R.id.tv_titulo_album);
            tvArtistName = itemView.findViewById(R.id.tv_titulo_song);
        }
    }
}