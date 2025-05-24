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
     * Создает новый плейлист в Spotify и добавляет в него треки.
     *
     * @param request запрос на создание плейлиста.
     * @throws Exception если произошла ошибка при создании плейлиста или добавлении треков.
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
     * Получает текущего пользователя из сессии.
     *
     * @return AppUser текущий пользователь.
     */
    private AppUser getCurrentUser() {
        return appUserRepo.findByUsername(session.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Создает экземпляр SpotifyApi с токеном доступа пользователя.
     *
     * @param user AppUser текущий пользователь.
     * @return SpotifyApi экземпляр API.
     */
    private SpotifyApi getSpotifyApi(AppUser user) {
        return new SpotifyApi.Builder()
                .setAccessToken(user.getSpotifyUser().getAccessToken())
                .build();
    }

    /**
     * Создает новый плейлист в Spotify.
     *
     * @param spotifyApi SpotifyApi экземпляр API.
     * @param user AppUser текущий пользователь.
     * @param request PlaylistRequest запрос на создание плейлиста.
     * @return ID созданного плейлиста.
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
     * Собирает URI треков из запросов на создание плейлиста.
     *
     * @param spotifyApi SpotifyApi экземпляр API.
     * @param request PlaylistRequest запрос на создание плейлиста.
     * @return Список URI треков.
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
     * Ищет URI трека по имени и имени исполнителя.
     *
     * @param spotifyApi SpotifyApi экземпляр API.
     * @param query TrackSearchQuery запрос на поиск трека.
     * @return URI найденного трека или null, если не найден.
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
     * Добавляет треки в плейлист.
     *
     * @param spotifyApi SpotifyApi экземпляр API.
     * @param playlistId ID плейлиста.
     * @param uris Список URI треков.
     */
    private void addTracksToPlaylist(SpotifyApi spotifyApi, String playlistId, List<String> uris) throws Exception {
        AddItemsToPlaylistRequest addRequest = spotifyApi
                .addItemsToPlaylist(playlistId, uris.toArray(String[]::new))
                .build();
        addRequest.execute();
    }

    /**
     * Сохраняет метаданные плейлиста в базе данных.
     *
     * @param user AppUser текущий пользователь.
     * @param playlistId ID плейлиста.
     * @param request PlaylistRequest запрос на создание плейлиста.
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

