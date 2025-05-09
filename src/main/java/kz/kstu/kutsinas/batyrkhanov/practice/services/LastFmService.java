package kz.kstu.kutsinas.batyrkhanov.practice.services;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class LastFmService {
    private final RestTemplate rest = new RestTemplate();
    private final String apiKey = System.getProperty("LAST_FM_API_KEY");
    private final SpotifyService spotifyService;

    public LastFmService(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    /**
     * Получает рекомендации на основе начальных сидов, жанра, региона и настроения.
     *
     * @param initialSeeds начальной сиды для генерации рекомендаций.
     * @param genre        жанр музыки.
     * @param region       регион.
     * @param mood         настроение.
     * @return список рекомендаций в виде TrackSearchQuery.
     */
    public List<TrackSearchQuery> getRecommendations(List<Track> initialSeeds, String genre, String region, String mood) {
        var recs = new ArrayList<TrackSearchQuery>();
        var seen = new HashSet<String>();

        List<Track> seeds = new ArrayList<>(initialSeeds);
        int attempts = 15;

        while (recs.size() < 50 && attempts-- > 0) {
            for (var track : seeds) {
                if (track.getArtists().length == 0) continue;
                var artist = track.getArtists()[0].getName();
                var name = track.getName();
                System.out.println("  > " + name + " / " + artist);

                for (var q : getSimilarTracks(artist, name, 10)) {
                    String key = q.getTrackName() + "|" + q.getArtistName();
                    if (!seen.add(key)) continue;

                    var found = spotifyService.searchTrack(q.getTrackName(), q.getArtistName()).orElse(null);
                    if (found != null)
                        recs.add(new TrackSearchQuery(found.getName(), found.getArtists()[0].getName()));

                    if (recs.size() >= 50) break;
                }
                if (recs.size() >= 50) break;
            }

            if (recs.size() < 50) {
                System.out.println("Недостаточно треков, получаем новые сиды...");
                seeds = spotifyService.getRandomTracks(genre, region, mood);
            }
        }

        return recs;
    }

    /**
     * Получает похожие треки на основе имени исполнителя и названия трека.
     *
     * @param artist имя исполнителя.
     * @param track  название трека.
     * @param limit  максимальное количество результатов.
     * @return список похожих треков в виде TrackSearchQuery.
     */
    private List<TrackSearchQuery> getSimilarTracks(String artist, String track, int limit) {
        try {
            System.out.printf("[LastFmService] Имена до запроса → artist: '%s', track: '%s'%n", artist, track);
            String url = String.format(
                    "https://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=%s&track=%s&autocorrect=1&api_key=%s&format=json&limit=%d",
                    artist, track, apiKey, limit
            );

            System.out.println("Запрос в Last.fm: " + url);

            JsonNode root = rest.getForObject(url, JsonNode.class);

            System.out.println("== JSON ===\n" + root.toPrettyString());

            if (root.has("error")) {
                System.out.println("Ошибка от Last.fm: " + root.path("message").asText());
                return List.of();
            }

            JsonNode items = root.path("similartracks").path("track");
            List<TrackSearchQuery> result = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode i : items) {
                    String name = i.path("name").asText();
                    String art = i.path("artist").path("name").asText();

                    if (!name.isEmpty() && !art.isEmpty()) {
                        result.add(new TrackSearchQuery(name, art));
                        System.out.printf("  → Добавлен: '%s' / '%s'%n", name, art);
                    }
                }
            } else {
                System.out.println("Поле 'track' не является массивом");
            }

            return result;

        } catch (Exception e) {
            System.err.println("Ошибка запроса к Last.fm: " + e.getMessage());
            return List.of();
        }
    }
}
