package com.example.appmusic_basico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;

import managers.PlaylistManager;
import models.Cancion_Reciente;
import models.SearchResultItem;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private static final String CLIENT_ID = "d4f8c9e33110499c895d46521552389c";
    private static final String REDIRECT_URI = "spotify-auth-app-basico://callback";
    private static final int REQUEST_CODE = 1337;
    private static final String TAG = "SpotifyMusicApp";
    public static SpotifyAppRemote mSpotifyAppRemote;
    public static String spotifyAccessToken = null;
    private static String mPendingSpotifyUri = null;

    private BottomNavigationView bottomNavigationView;
    private View miniPlayerBar;
    private TextView miniPlayerTrackTitle;
    private ImageButton miniPlayerPlayPauseButton;
    private Subscription<PlayerState> mPlayerStateSubscription;

    // Variables globales
    public static int currentSongIndex = 0;
    public static boolean isMusicPlaying = false;
    public static List<String> playlistUris = new ArrayList<>();
    public static List<Cancion_Reciente> globalPlaylist = new ArrayList<>();
    public static PlaylistManager playlistManager;
    // 💡 Nuevo: Bandera estática para solicitar reconexión
    public static boolean shouldReconnectSpotify = false;

    private FragmentHome fragmentHome;
    private FragmentFavourite fragmentFavourite;
    private FragmentSearch fragmentSearch;
    private FragmentProfile fragmentProfile;
    private Fragment activeFragment;
    private Track currentTrack;
    private boolean isPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar fragments
        fragmentHome = new FragmentHome();
        fragmentFavourite = new FragmentFavourite();
        fragmentSearch = new FragmentSearch();
        fragmentProfile = new FragmentProfile();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragmentProfile, "ProfileFragment").hide(fragmentProfile)
                .add(R.id.fragment_container, fragmentFavourite, "FavouriteFragment").hide(fragmentFavourite)
                .add(R.id.fragment_container, fragmentSearch, "SearchFragment").hide(fragmentSearch)
                .add(R.id.fragment_container, fragmentHome, "HomeFragment")
                .commit();

        activeFragment = fragmentHome;

        bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Logs de sanity
        Log.e("DEBUG_LAYOUT", "Post-setContentView reached");

        // Tomar directamente las vistas del mini player
        miniPlayerBar = findViewById(R.id.mini_player_bar);
        miniPlayerTrackTitle = findViewById(R.id.mini_player_track_title);
        miniPlayerPlayPauseButton = findViewById(R.id.mini_player_play_pause);

        // Validaciones para no crashear si alguna variante de layout no lo tiene
        if (miniPlayerBar == null || miniPlayerTrackTitle == null || miniPlayerPlayPauseButton == null) {
            Log.e("DEBUG_LAYOUT", "❌ mini player views NOT found in this layout variant");
        } else {
            Log.e("DEBUG_LAYOUT", "✅ mini player views OK");

            miniPlayerPlayPauseButton.setOnClickListener(v -> togglePlayPause());

            miniPlayerBar.setOnClickListener(v -> {
                if (currentTrack == null) return;
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                intent.putExtra("TRACK_NAME", currentTrack.name);
                intent.putExtra("ARTIST_NAME", currentTrack.artist.name);
                intent.putExtra("TRACK_URI", currentTrack.uri);
                intent.putExtra("IS_PLAYING", isPlaying);
                startActivity(intent);
            });
        }


        // Autenticación Spotify
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
            connectSpotifyRemote(true);
        }

        // 💡 Verifica si ThirdActivity solicitó una reconexión
        if (shouldReconnectSpotify) {
            shouldReconnectSpotify = false; // Restablecer la bandera
            reconnectSpotifyIfNecessary();
        }
    }

    // 💡 Nuevo: Método de reconexión de instancia
    public void reconnectSpotifyIfNecessary() {
        if (mSpotifyAppRemote == null || !mSpotifyAppRemote.isConnected()) {
            connectSpotifyRemote(false); // Llama a tu método de instancia
        }
    }

    private void updateMiniPlayerState(PlayerState playerState) {
        // Obtener la canción que se está reproduciendo
        Track currentTrack = playerState.track;

        // Actualizar UI del mini reproductor con el nombre de la canción y el artista
        if (currentTrack != null) {
            miniPlayerTrackTitle.setText(currentTrack.name + " - " + currentTrack.artist.name);

            // Cambiar el ícono de Play/Pause según el estado de la canción (si está en pausa o reproduciéndose)
            int playPauseIcon = playerState.isPaused ? R.drawable.play_arrow_24dp : R.drawable.pause_24dp;
            miniPlayerPlayPauseButton.setImageResource(playPauseIcon);
            miniPlayerBar.setVisibility(View.VISIBLE);  // Asegurar que el mini reproductor sea visible
        } else {
            miniPlayerBar.setVisibility(View.GONE);  // Si no hay ninguna canción, esconder el mini reproductor
        }
    }



    // Fragment navigation
    private void showFragment(Fragment fragmentToShow) {
        if (fragmentToShow == activeFragment) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(activeFragment);
        ft.show(fragmentToShow);
        ft.commit();
        activeFragment = fragmentToShow;
    }

    private void loadHomeFragment() {
        showFragment(fragmentHome);
    }

    public void triggerHomeContentLoad() {
        if (spotifyAccessToken != null && fragmentHome != null) {
            fragmentHome.cargarCancionesRecientes();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragmentToShow = null;
        int id = item.getItemId();
        if (id == R.id.nav_home) fragmentToShow = fragmentHome;
        else if (id == R.id.nav_search) fragmentToShow = fragmentSearch;
        else if (id == R.id.nav_favorite) fragmentToShow = fragmentFavourite;
        else if (id == R.id.nav_profile) fragmentToShow = fragmentProfile;

        if (fragmentToShow != null) {
            showFragment(fragmentToShow);
            return true;
        }
        return false;
    }

    // Spotify Authentication
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
        AuthorizationRequest request = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(scopes).build();
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
                    connectSpotifyRemote(true);
                    break;
                case ERROR:
                    Toast.makeText(this, "Error de autenticación: " + response.getError(), Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(this, "Autenticación cancelada.", Toast.LENGTH_SHORT).show();
            }
        }
        triggerHomeContentLoad();
    }

    // Spotify App Remote
    public void connectSpotifyRemote(boolean showAuthView) {
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(showAuthView)
                .build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d(TAG, "✅ Spotify App Remote conectado.");

                // 💡 CAMBIO CLAVE: Inicializar o configurar el PlaylistManager AQUÍ
                if (playlistManager == null) {
                    // Inicializar si es la primera vez
                    playlistManager = new PlaylistManager(globalPlaylist, mSpotifyAppRemote);
                } else {
                    // Actualizar la referencia del Remote si ya existía
                    playlistManager.setSpotifyAppRemote(mSpotifyAppRemote);
                }

                subscribeToPlayerStateInMain();

                // Si hay alguna URI pendiente para reproducir
                if (mPendingSpotifyUri != null) {
                    String uriToPlay = mPendingSpotifyUri;
                    mPendingSpotifyUri = null;
                    // Usar el manager para reproducir
                    if (playlistManager != null) {
                        playlistManager.playUri(uriToPlay);
                    } else {
                        mSpotifyAppRemote.getPlayerApi().play(uriToPlay);
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "❌ Error al conectar App Remote: " + throwable.getMessage(), throwable);
            }
        });
    }


    // Mini Player subscription
    private void subscribeToPlayerStateInMain() {
        if (mSpotifyAppRemote == null) return;

        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription.cancel();
            mPlayerStateSubscription = null;
        }

        mPlayerStateSubscription = (Subscription<PlayerState>) mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    currentTrack = playerState.track;
                    isPlaying = !playerState.isPaused;
                    updateMiniPlayerState(playerState);  // Actualiza el mini reproductor
                })
                .setErrorCallback(error -> Log.e(TAG, "Error PlayerState subscription: " + error.getMessage()));
    }



    // Playback
    private void togglePlayPause() {
        if (mSpotifyAppRemote == null) return;
        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) mSpotifyAppRemote.getPlayerApi().resume();
            else mSpotifyAppRemote.getPlayerApi().pause();
        });
    }

    // Este método debe delegar la reproducción al PlaylistManager
    public void playSpotifyUri(String uri) {
        if (mSpotifyAppRemote == null) {
            // La lógica de reconexión debe quedarse aquí en MainActivity
            mPendingSpotifyUri = uri;
            connectSpotifyRemote(false);
            return;
        }

        // 💡 CAMBIO CLAVE: Usar el PlaylistManager para iniciar la reproducción.
        // Esto es vital si quieres que el Manager actualice su estado interno (index, etc.).
        if (playlistManager != null) {
            playlistManager.playUri(uri);
        } else {
            // Fallback si por alguna razón el manager es null
            mSpotifyAppRemote.getPlayerApi().play(uri)
                    .setResultCallback(empty -> Log.d(TAG, "✅ Reproducción iniciada con URI: " + uri))
                    .setErrorCallback(error -> Log.e(TAG, "❌ Fallo al reproducir: " + error.getMessage()));
        }
    }

    public void trackPlayed(SearchResultItem track) {
        // 1. Crear una nueva Cancion_Reciente
        Cancion_Reciente nuevaCancion = new Cancion_Reciente(
                track.getTitle(),
                track.getSubtitle(), // Nombre del artista
                track.getImageUrl(),
                track.getSpotifyUri(),
                true
        );

        // 2. Añadirla a la lista global.
        // Lo ideal es primero eliminar duplicados si existe (para que sea la más reciente)
        globalPlaylist.removeIf(c -> c.getSpotifyUri().equals(track.getSpotifyUri()));
        globalPlaylist.add(0, nuevaCancion); // Agregar al inicio (más reciente)

        // 3. Notificar a FragmentHome para que actualice la vista de recientes
        if (fragmentHome != null) {
            fragmentHome.cargarCancionesRecientes();
        }
    }

    public static SpotifyAppRemote getSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }
}
