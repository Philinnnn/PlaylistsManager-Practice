package kz.kstu.kutsinas.batyrkhanov.practice.services;

import kz.kstu.kutsinas.batyrkhanov.practice.dto.PlaylistRequest;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.PlaylistsRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final AppUserRepo appUserRepo;
    private final PlaylistsRepo playlistsRepo;
    private final UserSession session;

    @Getter
    private String lastPlaylistId;

    /**
     * Creates a new playlist in Spotify and adds tracks to it.
     *
     * @param request request for the creation of a playlist.
     * @throws Exception If an error occurred when creating a playlist or adding tracks.
     */
    public void createPlaylist(PlaylistRequest request) throws Exception {
        AppUser user = getCurrentUser();
        SpotifyApi spotifyApi = getSpotifyApi(user);

        String playlistId = createSpotifyPlaylist(spotifyApi, user, request);
        this.lastPlaylistId = playlistId;
        List<String> trackUris = collectTrackUris(spotifyApi, request);

        if (!trackUris.isEmpty()) {
            addTracksToPlaylist(spotifyApi, playlistId, trackUris);
        }

        savePlaylistMetadata(user, playlistId, request);
    }

    // === PRIVATE METHODS ===

   /**
     * Receives the current user from the session.
     *
     * @return AppUser current user.
     */
    private AppUser getCurrentUser() {
        return appUserRepo.findByUsername(session.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Creates SpotifyApi copy with user access tokens.
     *
     * @param user AppUser current user.
     * @return SpotifyApi API copy.
     */
    private SpotifyApi getSpotifyApi(AppUser user) {
        return new SpotifyApi.Builder()
                .setAccessToken(user.getSpotifyUser().getAccessToken())
                .build();
    }

    /**
     * Creates a new playlist in Spotify.
     *
     * @param spotifyApi spotifyApi API copy.
     * @param user AppUser current user.
     * @param request PlaylistRequest request for the creation of a playlist.
     * @return ID of the created playlist.
     */
    private String createSpotifyPlaylist(SpotifyApi spotifyApi, AppUser user, PlaylistRequest request) throws Exception {
        CreatePlaylistRequest createRequest = spotifyApi
                .createPlaylist(user.getSpotifyUser().getId(), request.getName())
                .description(request.getDescription())
                .public_(request.getIsPublic())
                .build();

        return createRequest.execute().getId();
    }

   /**
     * Collects URI tracks from requests to create a playlist.
     *
     * @param spotifyApi SpotifyApi API copy.
     * @param request PlaylistRequest A request to create a playlist.
     * @return List of URI tracks.
     */
    private List<String> collectTrackUris(SpotifyApi spotifyApi, PlaylistRequest request) {
        List<String> uris = new ArrayList<>();
        for (TrackSearchQuery query : request.getTrackRequests()) {
            System.out.println("🎵 Отправляю в Spotify: " + query.getTrackName() + " — " + query.getArtistName());
            String uri = searchTrackUri(spotifyApi, query);
            if (uri != null) {
                uris.add(uri);
            } else {
                System.out.println("⚠ Не найден: " + query.getTrackName() + " — " + query.getArtistName());
            }
        }
        return uris;
    }

    /**
     * Searches for the URI track named and the name of the performer.
     *
     * @param spotifyApi SpotifyApi API copy.
     * @param query TrackSearchQuery Request for the search for the track.
     * @return URI Found track or null, if not found.
     */
    private String searchTrackUri(SpotifyApi spotifyApi, TrackSearchQuery query) {
        try {
            SearchTracksRequest searchRequest = spotifyApi
                    .searchTracks("track:" + query.getTrackName() + " artist:" + query.getArtistName())
                    .limit(1)
                    .build();

            Track[] tracks = searchRequest.execute().getItems();
            if (tracks.length > 0) {
                return tracks[0].getUri();
            }
        } catch (Exception e) {
            System.err.println("Ошибка поиска трека: " + query + " — " + e.getMessage());
        }
        return null;
    }

    /**
     * Adds tracks to the playlist.
     *
     * @param spotifyApi SpotifyApi API copy.
     * @param playlistId ID playlist.
     * @param uris List of URI tracks.
     */
    private void addTracksToPlaylist(SpotifyApi spotifyApi, String playlistId, List<String> uris) throws Exception {
        AddItemsToPlaylistRequest addRequest = spotifyApi
                .addItemsToPlaylist(playlistId, uris.toArray(String[]::new))
                .build();
        addRequest.execute();
    }

    /**
     * Saves metadata playlist in the database.
     *
     * @param user AppUser current user.
     * @param playlistId  playlist ID.
     * @param request PlaylistRequest A request to create a playlist.
     */
    private void savePlaylistMetadata(AppUser user, String playlistId, PlaylistRequest request) {
        Playlist playlist = new Playlist(
                playlistId,
                request.getName(),
                request.getDescription(),
                request.getIsPublic(),
                user
        );
        playlistsRepo.save(playlist);
    }


}

