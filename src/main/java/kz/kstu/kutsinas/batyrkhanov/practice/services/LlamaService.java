package kz.kstu.kutsinas.batyrkhanov.practice.services;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LlamaService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${LLAMA_HOST}")
    private String llamaHost;

    @Value("${LLAMA_MODEL}")
    private String llamaModel;

  /**
     * Selection of the 50 best tracks based on preferences (SEED tracies + candidate trees).
     */
    public List<TrackSearchQuery> selectBestTracks(List<TrackSearchQuery> seeds, List<TrackSearchQuery> candidates) {
        StringBuilder prompt = new StringBuilder("Вот список треков, которые вы любите:\n");
        for (TrackSearchQuery seed : seeds) {
            prompt.append(seed.getArtistName()).append(" — ").append(seed.getTrackName()).append("\n");
        }

        prompt.append("\nВот большой список других песен:\n");
        for (TrackSearchQuery candidate : candidates) {
            prompt.append(candidate.getArtistName()).append(", ").append(candidate.getTrackName()).append("\n");
        }

        prompt.append("\nВыбери 50 лучших треков, максимально похожих на первые. Формат: Артист, Название. Только список.");

        return parseResponseToTrackList(queryLlama(prompt.toString()), 50);
    }

   /**
     * Selection of the 50 best tracks based on the genre, region and mood.
     */
    public List<TrackSearchQuery> selectBestTracks(List<TrackSearchQuery> tracks, String genre, String region, String mood) {
        StringBuilder prompt = new StringBuilder("Из этого списка выбери 50 треков");

        if (genre != null && !genre.isBlank()) {
            prompt.append(", которые лучше всего соответствуют жанру ").append(genre);
        }
        if (region != null && !region.isBlank()) {
            prompt.append(", региону ").append(region);
        }
        if (mood != null && !mood.isBlank()) {
            prompt.append(", настроению ").append(mood);
        }

        prompt.append(". Только список в формате: Артист, Название.\n\n");

        for (TrackSearchQuery track : tracks) {
            prompt.append(track.getArtistName()).append(", ").append(track.getTrackName()).append("\n");
        }

        return parseResponseToTrackList(queryLlama(prompt.toString()), 50);
    }

    /**
     * Generation of artists by genre, region, mood.
     */
    public List<String> getArtistNames(String genre, String region, String mood) {
        String prompt = String.format(
                "Назови 20 %s%s%s исполнителей. Только имена. Без номеров, кавычек и пояснений.",
                mood != null ? mood + " " : "",
                genre != null ? genre + " " : "",
                region != null ? "из региона " + region : ""
        ).trim();

        System.out.println("[LlamaService] Prompt (by tags): " + prompt);
        return parseResponseToArtistList(queryLlama(prompt));
    }

    /**
     * Request to the local API LLAMA.
     */
    private List<String> queryLlama(String prompt) {
        String url = llamaHost + "/api/generate";
        Map<String, Object> body = Map.of(
                "model", llamaModel,
                "prompt", prompt,
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            JsonNode json = restTemplate.postForObject(url, request, JsonNode.class);
            String responseText = json.path("response").asText();
            System.out.println("[LlamaService] Ответ: \n" + responseText);
            return List.of(responseText.split("\\r?\\n"));
        } catch (Exception e) {
            System.err.println("Ошибка запроса к LLaMA: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Parsing line "Artist, name".
     */
    private Optional<TrackSearchQuery> parseLine(String line) {
        String cleaned = line.trim().replaceFirst("^\\d+[.)]?\\s*", "");

        String[] parts = null;
        if (cleaned.contains(",")) {
            parts = cleaned.split(",", 2);
        } else if (cleaned.contains(" - ")) {
            parts = cleaned.split(" - ", 2);
        } else if (cleaned.contains(" — ")) {
            parts = cleaned.split(" — ", 2);
        }

        if (parts == null || parts.length != 2) return Optional.empty();

        String artist = parts[0].trim().replaceAll("^\"|\"$", "");
        String track = parts[1].trim().replaceAll("^\"|\"$", "");

        if (artist.isEmpty() || track.isEmpty() || artist.length() > 80 || track.length() > 120) {
            return Optional.empty();
        }

        return Optional.of(new TrackSearchQuery(track, artist));
    }

    /**
     * Llama Parsing in the list of tracks.
     */
    private List<TrackSearchQuery> parseResponseToTrackList(List<String> lines, int limit) {
        List<TrackSearchQuery> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String line : lines) {
            parseLine(line).ifPresent(q -> {
                String key = q.getTrackName() + "|" + q.getArtistName();
                if (seen.add(key)) {
                    result.add(q);
                }
            });
            if (result.size() >= limit) break;
        }

        return result;
    }

   /**
     * Parsing of artists from Llama-answer.
     */
    private List<String> parseResponseToArtistList(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            String cleaned = line.replaceFirst("^\\d+[.)]?\\s*", "").trim();
            if (!cleaned.isBlank() && cleaned.length() < 80) {
                result.add(cleaned);
            }
        }
        return result;
    }
}
