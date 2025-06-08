package kz.kstu.kutsinas.batyrkhanov.practice.services;

import com.neovisionaries.i18n.CountryCode;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final AppUserRepo appUserRepo;
    private final SpotifyUsersRepo usersRepo;
    private final UserSession session;
    private final AuthService authService;

   /**
     * Establishes a connection between the user of the application and his Spotify account.
     *
     * @param authentication OAuth2AuthenticationToken, Received from Spring Security after successful authentication.
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
     * Receives an access or update token for the specified type.
     *
     * @param auth OAuth2AuthenticationToken, received from Spring Security after successful authentication.
     * @param type Token type (ACCESS or REFRESH).
     * @return The value of token.
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
     * Retrieves the top tracks of a specified artist from Spotify.
     *
     * @param artistName The name of the artist.
     * @param region The region code (e.g., "US", "GB"). If null or empty, defaults to "US".
     * @return A list of TrackSearchQuery objects containing track names and artist names.
     */
    public List<TrackSearchQuery> getTopTracksByArtist(String artistName, String region) {
        List<TrackSearchQuery> tracks = new ArrayList<>();
        if(region == null || region.isEmpty()) {
            region = "US";
        }
        CountryCode code = CountryCode.getByAlpha2Code(region.toUpperCase());
        try {
            String accessToken = session.getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Spotify access token not found in session");
            }
            var spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(accessToken)
                    .build();
            var artistSearch = spotifyApi.searchArtists(artistName).limit(1).build().execute().getItems();
            if (artistSearch.length == 0) return tracks;
            var artist = artistSearch[0];
            var topTracksRequest = spotifyApi.getArtistsTopTracks(artist.getId(), code);
            var topTracks = topTracksRequest.build().execute();
            for (Track track : topTracks) {
                tracks.add(new TrackSearchQuery(track.getName(), artist.getName()));
            }
            return tracks;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("invalid access token")) {
                try {
                    AppUser user = appUserRepo.findByUsername(session.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                    authService.refreshSpotifyAccessToken(user);
                    session.setAccessToken(user.getSpotifyUser().getAccessToken());
                    return getTopTracksByArtist(artistName, region);
                } catch (Exception ex) {
                    System.err.println("Ошибка при рефреше access token: " + ex.getMessage());
                }
            }
            System.err.println("Ошибка при получении топ-треков артиста: " + artistName + " — " + e.getMessage());
            return tracks;
        }
    }

    /**
     * Retrieves the user's top tracks from Spotify.
     *
     * @return A list of TrackSearchQuery objects containing track names and artist names.
     */
    public List<TrackSearchQuery> getUserTopTracks() {
        try {
            String accessToken = session.getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Spotify access token not found in session");
            }
            SpotifyApi api = new SpotifyApi.Builder()
                    .setAccessToken(accessToken)
                    .build();
            Paging<Track> trackPaging = api.getUsersTopTracks()
                    .limit(50)
                    .build()
                    .execute();
            if (trackPaging == null || trackPaging.getItems() == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(trackPaging.getItems())
                    .filter(Objects::nonNull)
                    .map(track -> {
                        String trackName = track.getName();
                        String artistName = (track.getArtists() != null && track.getArtists().length > 0)
                                ? track.getArtists()[0].getName()
                                : "Unknown Artist";
                        return new TrackSearchQuery(trackName, artistName);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("invalid access token")) {
                try {
                    AppUser user = appUserRepo.findByUsername(session.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                    authService.refreshSpotifyAccessToken(user);
                    session.setAccessToken(user.getSpotifyUser().getAccessToken());
                    return getUserTopTracks();
                } catch (Exception ex) {
                    System.err.println("Ошибка при рефреше access token: " + ex.getMessage());
                }
            }
            System.err.println("Ошибка при получении топ-треков пользователя: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves the Spotify track ID for a given artist and track name.
     *
     * @param artistName The name of the artist.
     * @param trackName The name of the track.
     * @return The Spotify track ID, or null if not found.
     */
    public String getTrackId(String artistName, String trackName) {
        try {
            String accessToken = session.getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Spotify access token not found in session");
            }
            SpotifyApi api = new SpotifyApi.Builder()
                    .setAccessToken(accessToken)
                    .build();
            var result = api.searchTracks(trackName + " artist:" + artistName).limit(1).build().execute();
            if (result.getItems().length > 0) {
                return result.getItems()[0].getId();
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("invalid access token")) {
                try {
                    AppUser user = appUserRepo.findByUsername(session.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                    authService.refreshSpotifyAccessToken(user);
                    session.setAccessToken(user.getSpotifyUser().getAccessToken());
                    return getTrackId(artistName, trackName);
                } catch (Exception ex) {
                    System.err.println("Ошибка при рефреше access token: " + ex.getMessage());
                }
            }
            System.err.println("Ошибка получения ID трека: " + e.getMessage());
        }
        return null;
    }
}