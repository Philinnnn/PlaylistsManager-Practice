package kz.kstu.kutsinas.batyrkhanov.practice.services;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LastFmService {
    private final RestTemplate rest = new RestTemplate();
    private final String apiKey = System.getProperty("LAST_FM_API_KEY");
    private final UserSession session;

    public LastFmService(UserSession session) {
        this.session = session;
    }

    public List<TrackSearchQuery> getRecommendations(List<Track> seeds, String genre, String region, String mood) {
        System.out.printf("[LastFmService] Старт генерации рекомендаций\n  - Жанр: %s\n  - Регион: %s\n  - Настроение: %s\n", genre, region, mood);
        List<TrackSearchQuery> recs = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(session.getAccessToken()).build();

        for (Track t : seeds) {
            if (t.getArtists().length == 0) continue;
            String artist = t.getArtists()[0].getName(), name = t.getName();
            System.out.println("  > Анализ seed-трека: " + name + " / " + artist);

            for (TrackSearchQuery q : getSimilarTracks(artist, name, 10)) {
                if (!seen.add(q.getTrackName() + "|" + q.getArtistName())) continue;
                Optional<Track> opt = searchSpotifyTrack(api, q.getTrackName(), q.getArtistName());
                if (opt.isEmpty()) continue;

                Track found = opt.get();
                AudioFeatures f = getAudioFeatures(api, found.getId());
                Artist a = getArtist(api, found.getArtists()[0].getId());

                boolean genreOk = genre == null || artistHasGenre(a, genre);
                boolean moodOk = mood == null || moodMatches(f, mood);

                System.out.printf("    ✓ %s / %s — жанр: %s, настроение: %s\n",
                        found.getName(), found.getArtists()[0].getName(),
                        genreOk ? "OK" : "NO", moodOk ? "OK" : "NO");

                if (genreOk && moodOk)
                    recs.add(new TrackSearchQuery(found.getName(), found.getArtists()[0].getName()));
            }
        }

        System.out.println("[LastFmService] Финальный список рекомендаций: " + recs.size() + " треков");
        return recs;
    }

    private List<TrackSearchQuery> getSimilarTracks(String artist, String track, int limit) {
        try {
            String url = String.format("https://ws.audioscrobbler.com/2.0/?method=track." +
                            "getsimilar" +
                            "&artist=%s" +
                            "&track=%s" +
                            "&autocorrect=1" +
                            "&api_key=%s" +
                            "&format=json" +
                            "&limit=%d",
                    URLEncoder.encode(artist, StandardCharsets.UTF_8),
                    URLEncoder.encode(track, StandardCharsets.UTF_8),
                    apiKey, limit);

            System.out.println("Запрос в Last.fm: " + url);
            JsonNode root = rest.getForObject(url, JsonNode.class);
            if (root.has("error")) {
                System.out.println("Ошибка от Last.fm: " + root.path("message").asText());
                return List.of();
            }

            JsonNode items = root.path("similartracks").path("track");
            List<TrackSearchQuery> queries = new ArrayList<>();
            items.forEach(i -> {
                String name = i.path("name").asText();
                String artistName = i.path("artist").path("name").asText();
                if (!name.isEmpty() && !artistName.isEmpty())
                    queries.add(new TrackSearchQuery(name, artistName));
            });
            return queries;
        } catch (Exception e) {
            System.err.println("Ошибка запроса к Last.fm: " + e.getMessage());
            return List.of();
        }
    }

    private Optional<Track> searchSpotifyTrack(SpotifyApi api, String track, String artist) {
        try {
            String q = "track:" + track + " artist:" + artist;
            System.out.println("Поиск в Spotify: " + q);
            Track[] items = api.searchTracks(q).limit(1).build().execute().getItems();
            return items.length > 0 ? Optional.of(items[0]) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private AudioFeatures getAudioFeatures(SpotifyApi api, String id) {
        try {
            return api.getAudioFeaturesForTrack(id).build().execute();
        } catch (Exception e) {
            return null;
        }
    }

    private Artist getArtist(SpotifyApi api, String id) {
        try {
            return api.getArtist(id).build().execute();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean artistHasGenre(Artist a, String genre) {
        return a != null && Arrays.stream(a.getGenres()).anyMatch(g -> g.equalsIgnoreCase(genre));
    }

    private boolean moodMatches(AudioFeatures f, String mood) {
        if (f == null) return false;
        return switch (mood.toLowerCase()) {
            case "happy" -> f.getValence() > 0.6 && f.getEnergy() > 0.5;
            case "sad" -> f.getValence() < 0.4 && f.getEnergy() < 0.5;
            case "energetic" -> f.getEnergy() > 0.7;
            case "calm" -> f.getEnergy() < 0.4 && f.getDanceability() < 0.5;
            default -> true;
        };
    }
}
