package com.example.appmusic_basico;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View; // Importación necesaria para el mini-reproductor
import android.widget.ImageButton; // Importación necesaria
import android.widget.TextView; // Importación necesaria
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// IMPORTACIONES SPOTIFY
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

import models.Cancion_Reciente;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    // 1. CONSTANTES CRÍTICAS
    private static final String CLIENT_ID = "d4f8c9e33110499c895d46521552389c";
    private static final String REDIRECT_URI = "spotify-auth-app-basico://callback";
    private static final int REQUEST_CODE = 1337;

    // Tags de Fragmentos
    private static final String HOME_FRAGMENT_TAG = "HomeFragment";
    private static final String SEARCH_FRAGMENT_TAG = "SearchFragment";
    private static final String FAVOURITE_FRAGMENT_TAG = "FavouriteFragment";
    private static final String PROFILE_FRAGMENT_TAG = "ProfileFragment";

    // ✅ DECLARACIONES SECUNDARIAS
    public static int currentSongIndex;
    public static boolean isMusicPlaying;
    public static List<String> playlistUris = new ArrayList<>();
    public static List<Cancion_Reciente> globalPlaylist = new ArrayList<>();
    public static MediaPlayer globalMediaPlayer;

    // 2. VARIABLES DE ESTADO
    private static SpotifyAppRemote mSpotifyAppRemote;
    public static String spotifyAccessToken = null;
    private static final String TAG = "SpotifyMusicApp";

    // Referencia a la barra de navegación
    private BottomNavigationView bottomNavigationView;

    // 💡 Referencias al Mini-Reproductor
    private View miniPlayerBar;
    private TextView miniPlayerTrackTitle;
    private ImageButton miniPlayerPlayPauseButton;

    // =========================================================================
    // 3. CICLO DE VIDA DE LA ACTIVIDAD
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar UI de navegación
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnItemSelectedListener(this);

        // 💡 2. Inicializar UI del Mini-Reproductor y Listeners
        miniPlayerBar = findViewById(R.id.mini_player_bar);
        miniPlayerTrackTitle = findViewById(R.id.mini_player_track_title);
        miniPlayerPlayPauseButton = findViewById(R.id.mini_player_play_pause);

        // Listener para alternar Play/Pause en el mini-reproductor
        miniPlayerPlayPauseButton.setOnClickListener(v -> togglePlayPause());

        // 3. Autenticación y carga inicial
        if (spotifyAccessToken == null) {
            authenticateSpotify();
        }

        if (savedInstanceState == null) {
            loadHomeFragment();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (spotifyAccessToken != null && mSpotifyAppRemote == null) {
            connectSpotifyRemote(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSpotifyAppRemote != null) {
            // Importante: Desconectar App Remote para liberar recursos
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mSpotifyAppRemote = null;
        }
    }

    // =========================================================================
    // 3.1 CARGA Y NAVEGACIÓN DE FRAGMENTOS
    // =========================================================================

    private void loadHomeFragment() {
        switchFragment(new FragmentHome(), HOME_FRAGMENT_TAG);
    }

    public void switchFragment(Fragment fragment, String tag) {
        Log.d(TAG, "Cambiando a Fragmento: " + tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String tag = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new FragmentHome();
            tag = HOME_FRAGMENT_TAG;
        } else if (item.getItemId() == R.id.nav_search) {
            selectedFragment = new FragmentSearch();
            tag = SEARCH_FRAGMENT_TAG;
        } else if (item.getItemId() == R.id.nav_favorite) {
            selectedFragment = new FragmentFavourite();
            tag = FAVOURITE_FRAGMENT_TAG;
        } else if (item.getItemId() == R.id.nav_profile){
            selectedFragment = new FragmentProfile();
            tag = PROFILE_FRAGMENT_TAG;
        }

        if (selectedFragment != null) {
            switchFragment(selectedFragment, tag);
            return true;
        }
        return false;
    }


    // =========================================================================
    // 4. FLUJO DE AUTENTICACIÓN SPOTIFY ANDROID SDK
    // =========================================================================

    public void authenticateSpotify() {
        String[] scopes = new String[]{
                "user-read-private",
                "playlist-read-private",
                "user-read-playback-state",
                "user-modify-playback-state",
                "app-remote-control",
                "streaming",
                "user-read-recently-played"
        };

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                        .setScopes(scopes);

        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    spotifyAccessToken = response.getAccessToken();
                    Toast.makeText(this, "Conexión Web API exitosa.", Toast.LENGTH_SHORT).show();
                    connectSpotifyRemote(false);

                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
                    if (fragment instanceof FragmentHome) {
                        ((FragmentHome) fragment).cargarCancionesRecientes();
                    }
                    break;

                case ERROR:
                    Log.e(TAG, "❌ Error de autenticación: " + response.getError());
                    Toast.makeText(this, "Error de conexión: " + response.getError(), Toast.LENGTH_LONG).show();
                    break;

                default:
                    Log.d(TAG, "Autenticación cancelada.");
                    Toast.makeText(this, "Conexión cancelada.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // =========================================================================
    // 5. CONEXIÓN SPOTIFY APP REMOTE & REPRODUCCIÓN
    // =========================================================================

    private void connectSpotifyRemote(boolean showAuthView) {
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(showAuthView)
                .build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d(TAG, "✅ Spotify App Remote conectado.");
                Toast.makeText(MainActivity.this, "App Remote conectado.", Toast.LENGTH_SHORT).show();

                // 💡 INICIA LA SUSCRIPCIÓN AL ESTADO DEL REPRODUCTOR
                subscribeToPlayerState();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "❌ Error al conectar App Remote: " + throwable.getMessage(), throwable);
                Toast.makeText(MainActivity.this, "App Remote falló: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Suscribe la aplicación al estado de reproducción actual de Spotify.
     * Esto actualiza la UI del mini-reproductor en tiempo real.
     */
    private void subscribeToPlayerState() {
        if (mSpotifyAppRemote == null) return;

        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            final com.spotify.protocol.types.Track track = playerState.track;

            if (track != null) {
                // 1. Mostrar la barra si no está visible
                if (miniPlayerBar.getVisibility() != View.VISIBLE) {
                    miniPlayerBar.setVisibility(View.VISIBLE);
                }

                // 2. Actualizar el título con el nombre de la canción y el artista
                String artistName = track.artist.name;
                miniPlayerTrackTitle.setText(track.name + " - " + artistName);

                // 3. Actualizar el icono de Play/Pause
                // Asume que R.drawable.play_arrow_24dp y R.drawable.pause_24dp existen
                if (playerState.isPaused) {
                    miniPlayerPlayPauseButton.setImageResource(R.drawable.play_arrow_24dp);
                } else {
                    miniPlayerPlayPauseButton.setImageResource(R.drawable.pause_24dp);
                }
            } else {
                // Ocultar el mini reproductor si no hay nada sonando o la App Remote se desconecta
                miniPlayerBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Alterna el estado de reproducción (Play/Pause) usando App Remote.
     */
    private void togglePlayPause() {
        if (mSpotifyAppRemote == null) {
            Toast.makeText(this, "App Remote no conectado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el estado actual y alternar la acción
        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) {
                mSpotifyAppRemote.getPlayerApi().resume();
            } else {
                mSpotifyAppRemote.getPlayerApi().pause();
            }
        });
    }

    public void playSongFromFragment(int songResourceId) {
        String spotifyUri = mapResourceIdToUri(songResourceId);

        if (mSpotifyAppRemote != null && spotifyUri != null) {
            playSpotifyUri(spotifyUri);
        } else if (mSpotifyAppRemote == null) {
            Log.e(TAG, "❌ App Remote no está conectado. No se puede iniciar la reproducción.");
            Toast.makeText(this, "Conéctate a Spotify primero.", Toast.LENGTH_SHORT).show();
            connectSpotifyRemote(false);
        }
    }

    private void playSpotifyUri(String uri) {
        mSpotifyAppRemote.getPlayerApi()
                .play(uri)
                .setResultCallback(emptyResult -> {
                    Log.d(TAG, "✅ Reproducción iniciada con URI: " + uri);
                    Toast.makeText(this, "Reproduciendo en Spotify", Toast.LENGTH_SHORT).show();
                })
                .setErrorCallback(throwable -> {
                    Log.e(TAG, "❌ Fallo al iniciar la reproducción: " + throwable.getMessage());
                    Toast.makeText(this, "Error al reproducir: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String mapResourceIdToUri(int id) {
        if (id == 0) {
            return "spotify:track:4cOdK2wGQHdAY5BVfeGDFi"; // Rick Astley - Never Gonna Give You Up
        }
        Log.w(TAG, "⚠️ ID de recurso no mapeado. Usando valor por defecto.");
        return "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M";
    }

    public static SpotifyAppRemote getSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }
}
