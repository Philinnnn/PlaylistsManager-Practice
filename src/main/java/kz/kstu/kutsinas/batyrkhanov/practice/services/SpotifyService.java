package kz.kstu.kutsinas.batyrkhanov.practice.services;

import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.SpotifyUser;
import kz.kstu.kutsinas.batyrkhanov.practice.enums.TokenType;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.SpotifyUsersRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final AppUserRepo appUserRepo;
    private final SpotifyUsersRepo usersRepo;
    private final UserSession session;

    /**
     * Устанавливает связь между пользователем приложения и его аккаунтом Spotify.
     *
     * @param authentication OAuth2AuthenticationToken, полученный от Spring Security после успешной аутентификации.
     */
    public void handleSpotifyCallback(OAuth2AuthenticationToken authentication) {
        String appUsername = session.getUsername();
        if (appUsername == null) {
            throw new RuntimeException("You must be logged in to link Spotify account.");
        }

        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

        String accessToken = getToken(authentication, TokenType.ACCESS);
        String refreshToken = getToken(authentication, TokenType.REFRESH);

        SpotifyUser spotifyUser = new SpotifyUser(attributes, accessToken, refreshToken);
        usersRepo.save(spotifyUser);

        AppUser appUser = appUserRepo.findByUsername(appUsername)
                .orElseThrow(() -> new RuntimeException("AppUser not found"));
        appUser.setSpotifyUser(spotifyUser);
        spotifyUser.setAppUser(appUser);
        appUserRepo.save(appUser);

        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);

        System.out.println("Session: " + session);
    }

    /**
     * Получает 5 случайных треков пользователя
     *
     * @return Список треков.
     */
    public List<Track> getRandomTracks(String genre, String region, String mood) {
        System.out.println("[SpotifyService] getRandomTracks() — старт");

        //TODO: добавить фильтрацию по жанру, региону и настроению

        String token = session.getAccessToken();
        if (token == null || token.isBlank()) {
            System.out.println("[SpotifyService] Access token отсутствует в сессии");
            throw new RuntimeException("Access token is missing");
        }

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(token)
                .build();

        try {
            System.out.println("[SpotifyService] Отправка запроса к Spotify API на получение топ-треков");

            Paging<Track> trackPaging = spotifyApi
                    .getUsersTopTracks()
                    .limit(50)
                    .build()
                    .execute();

            List<Track> allTracks = new ArrayList<>(Arrays.asList(trackPaging.getItems()));
            System.out.println("[SpotifyService] Получено треков: " + allTracks.size());

            Collections.shuffle(allTracks);
            List<Track> selected = allTracks.stream().limit(5).toList();

            System.out.println("[SpotifyService] Выбраны 5 случайных треков:");
            for (Track track : selected) {
                String name = track.getName();
                String artist = track.getArtists().length > 0 ? track.getArtists()[0].getName() : "Неизвестен";
                System.out.println("  - " + name + " / " + artist);
            }
            return selected;
        } catch (Exception e) {
            System.out.println("[SpotifyService] Ошибка при получении топ-треков: " + e.getMessage());
            throw new RuntimeException("Error fetching top tracks: " + e.getMessage());
        }
    }

    /**
     * Получает токен доступа или обновления для указанного типа.
     *
     * @param auth OAuth2AuthenticationToken, полученный от Spring Security после успешной аутентификации.
     * @param type Тип токена (ACCESS или REFRESH).
     * @return Значение токена.
     */
    private String getToken(OAuth2AuthenticationToken auth, TokenType type) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                auth.getAuthorizedClientRegistrationId(), auth.getName()
        );

        return switch (type) {
            case ACCESS -> client.getAccessToken().getTokenValue();
            case REFRESH -> client.getRefreshToken() != null
                    ? client.getRefreshToken().getTokenValue()
                    : null;
        };
    }

    /**
     * Ищет трек в Spotify по имени и имени исполнителя.
     *
     * @param trackName  Имя трека.
     * @param artistName Имя исполнителя.
     * @return Найденный трек или пустое значение, если не найден.
     */
    public Optional<Track> searchTrack(String trackName, String artistName) {
        try {
            String query = "track:" + trackName + " artist:" + artistName;
            System.out.println("[SpotifyService] Поиск в Spotify: " + query);

            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(session.getAccessToken())
                    .build();

            // Основной точный запрос
            Track[] result = spotifyApi.searchTracks(query).limit(5).build().execute().getItems();

            if (result.length > 0) {
                // Проверка на близкое совпадение по названию
                for (Track track : result) {
                    if (track.getName().toLowerCase().contains(trackName.toLowerCase())) {
                        System.out.println("✅ Найден (частичное совпадение): " + track.getName() + " — " + track.getArtists()[0].getName());
                        return Optional.of(track);
                    }
                }

                // Если нет точного совпадения, берём первый
                System.out.println("⚠ Взяли первое приближённое совпадение: " + result[0].getName() + " — " + result[0].getArtists()[0].getName());
                return Optional.of(result[0]);
            }

            System.out.println("❌ Ничего не найдено по: " + trackName + " — " + artistName);
            return Optional.empty();

        } catch (Exception e) {
            System.err.println("🚫 Ошибка при поиске трека в Spotify: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<TrackSearchQuery> getRandomTrackByArtist(String artistName) {
        try {
            var spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(session.getAccessToken())
                    .build();

            // Найдём артиста
            var artistSearch = spotifyApi.searchArtists(artistName).limit(1).build().execute().getItems();
            if (artistSearch.length == 0) return Optional.empty();

            var artist = artistSearch[0];

            // Получим альбомы
            var albums = spotifyApi.getArtistsAlbums(artist.getId()).limit(10).build().execute().getItems();
            if (albums.length == 0) return Optional.empty();

            var randomAlbum = albums[new Random().nextInt(albums.length)];

            // Получим треки из альбома
            var albumTracks = spotifyApi.getAlbumsTracks(randomAlbum.getId()).limit(10).build().execute().getItems();
            if (albumTracks.length == 0) return Optional.empty();

            var randomTrack = albumTracks[new Random().nextInt(albumTracks.length)];
            return Optional.of(new TrackSearchQuery(randomTrack.getName(), artist.getName()));
        } catch (Exception e) {
            System.err.println("[SpotifyService] Ошибка при получении трека по артисту: " + artistName + " — " + e.getMessage());
            return Optional.empty();
        }
    }
}