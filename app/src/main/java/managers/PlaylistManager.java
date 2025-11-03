package managers;

import android.util.Log;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import models.Cancion_Reciente;
import java.util.List;
import com.spotify.protocol.types.Repeat;

public class PlaylistManager {

    private static final String TAG = "PlaylistManager";
    private List<Cancion_Reciente> playlist;
    private SpotifyAppRemote spotifyAppRemote;

    // 💡 Cambio 1: El índice actual es una variable de INSTANCIA, no estática.
    private int currentSongIndex = -1;

    // Constructor corregido
    public PlaylistManager(List<Cancion_Reciente> playlist, SpotifyAppRemote spotifyAppRemote) {
        this.playlist = playlist;
        this.spotifyAppRemote = spotifyAppRemote;
        // 💡 Inicializa el índice al iniciar, si la lista no está vacía.
        if (playlist != null && !playlist.isEmpty()) {
            this.currentSongIndex = 0;
        }
    }

    // Establecer o actualizar la playlist
    public void setPlaylist(List<Cancion_Reciente> playlist) {
        this.playlist = playlist;
        // 💡 Reiniciar el índice cuando la playlist se actualiza
        this.currentSongIndex = (playlist != null && !playlist.isEmpty()) ? 0 : -1;
    }

    // Método para actualizar el SpotifyAppRemote
    public void setSpotifyAppRemote(SpotifyAppRemote spotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote;
    }

    // Método para establecer el índice actual desde afuera (útil al iniciar)
    public void setCurrentSongIndex(int index) {
        if (playlist != null && index >= 0 && index < playlist.size()) {
            this.currentSongIndex = index;
        } else {
            Log.e(TAG, "Índice inválido para la playlist.");
        }
    }

    // Método para obtener el índice actual (si es necesario para la UI)
    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    // Reproducir una canción en base al índice
    public void playSongAtIndex(int index) {
        if (spotifyAppRemote == null || playlist == null || index < 0 || index >= playlist.size()) {
            Log.e(TAG, "No se puede reproducir la canción: datos inválidos.");
            return;
        }

        Cancion_Reciente cancion = playlist.get(index);
        String spotifyUri = cancion.getSpotifyUri();

        // 💡 Cambio 2: Actualiza el índice ANTES de reproducir.
        this.currentSongIndex = index;

        // Reproducir la canción desde el URI
        spotifyAppRemote.getPlayerApi().play(spotifyUri)
                .setResultCallback(empty -> {
                    Log.d(TAG, "✅ Reproduciendo canción: " + cancion.getTitulo());
                })
                .setErrorCallback(throwable -> {
                    Log.e(TAG, "❌ Error al reproducir la canción: " + throwable.getMessage());
                });
    }

    // Método para reproducir URI específico (playUri)
    public void playUri(String uri) {
        if (spotifyAppRemote == null) {
            Log.e(TAG, "❌ SpotifyAppRemote no conectado.");
            return;
        }

        // 1. Intentar encontrar el índice correspondiente a este URI
        int foundIndex = -1;
        if (playlist != null) {
            for (int i = 0; i < playlist.size(); i++) {
                // Verifica si el URI de la canción en la playlist coincide con el URI a reproducir
                if (uri.equals(playlist.get(i).getSpotifyUri())) {
                    foundIndex = i;
                    break;
                }
            }
        }

        // 2. Si el URI fue encontrado en la lista local, actualiza el índice
        if (foundIndex != -1) {
            this.currentSongIndex = foundIndex;
            Log.d(TAG, "Índice de la playlist actualizado a: " + this.currentSongIndex);
        } else {
            // Opcional: Si se reproduce una canción que no está en la lista actual,
            // puedes dejar el índice como -1 para indicar que está fuera de la secuencia
            // o dejarlo sin modificar para no interrumpir el flujo si es una lista temporal.
            // Lo dejaremos sin modificar si la canción no está en la lista.
        }

        // 3. Reproducir el URI
        spotifyAppRemote.getPlayerApi().play(uri);
        Log.d(TAG, "Reproduciendo URI específico: " + uri);
    }

    // Método para reproducir la siguiente canción (skipNext)
    public void playNext() {
        if (spotifyAppRemote == null || playlist == null || playlist.isEmpty()) {
            Log.e(TAG, "❌ No hay canciones en la lista o SpotifyAppRemote no conectado.");
            return;
        }

        // Obtener el índice de la canción actual (ya es de la instancia)
        // Si el índice es -1 (lista vacía o recién inicializada), forzamos a 0
        int current = (currentSongIndex == -1) ? 0 : currentSongIndex;

        // Avanzar al siguiente índice (manejando el caso de llegar al final de la lista)
        int nextSongIndex = (current + 1) % playlist.size();

        // Reproducir la siguiente canción (que internamente actualiza this.currentSongIndex)
        playSongAtIndex(nextSongIndex);
    }

    // Método para reproducir la canción anterior (skipPrevious)
    public void playPrevious() {
        if (spotifyAppRemote == null || playlist == null || playlist.isEmpty()) {
            Log.e(TAG, "❌ No hay canciones en la lista o SpotifyAppRemote no conectado.");
            return;
        }

        // Obtener el índice de la canción actual (ya es de la instancia)
        int current = (currentSongIndex == -1) ? 0 : currentSongIndex;

        // Retroceder al índice anterior (manejando el caso de llegar al principio de la lista)
        int previousSongIndex = (current - 1 + playlist.size()) % playlist.size();

        // Reproducir la canción anterior (que internamente actualiza this.currentSongIndex)
        playSongAtIndex(previousSongIndex);
    }

    // Reproducir la canción actual si está pausada
    public void togglePlayPause() {
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().getPlayerState()
                    .setResultCallback(playerState -> {
                        if (playerState.isPaused) {
                            spotifyAppRemote.getPlayerApi().resume();
                        } else {
                            spotifyAppRemote.getPlayerApi().pause();
                        }
                    });
        } else {
            Log.e(TAG, "❌ SpotifyAppRemote no conectado.");
        }
    }

    public void toggleRepeat() {
        if (spotifyAppRemote == null) {
            Log.e(TAG, "❌ SpotifyAppRemote no conectado para alternar la repetición.");
            return;
        }

        spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            int nextRepeatMode;

            // Ciclo: ALL (playlist) -> ONE (canción) -> OFF (desactivado)
            if (playerState.playbackOptions.repeatMode == Repeat.ALL) {
                nextRepeatMode = Repeat.ONE;
            } else if (playerState.playbackOptions.repeatMode == Repeat.ONE) {
                nextRepeatMode = Repeat.OFF;
            } else {
                nextRepeatMode = Repeat.ALL;
            }

            // Ejecutar la acción en Spotify
            spotifyAppRemote.getPlayerApi().setRepeat(nextRepeatMode)
                    .setResultCallback(empty -> {
                        Log.d(TAG, "✅ Modo de repetición cambiado a: " + nextRepeatMode);
                        // NOTA: La ThirdActivity se encargará del Toast.
                    })
                    .setErrorCallback(throwable -> {
                        Log.e(TAG, "❌ Error al establecer el modo de repetición: " + throwable.getMessage());
                    });
        });
    }
}