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

    public List<TrackSearchQuery> selectTopTracks(List<TrackSearchQuery> tracks, String genre, String region, String mood) {
        StringBuilder sb = new StringBuilder("Из этого списка выбери 50 треков, которые лучше всего соответствуют жанру ");
        sb.append(genre != null ? genre : "любой").append(", региону ")
                .append(region != null ? region : "любой").append(", настроению ")
                .append(mood != null ? mood : "любому").append(". Только список в формате: Артист, Название\n");

        for (TrackSearchQuery track : tracks) {
            sb.append(track.getArtistName()).append(", ").append(track.getTrackName()).append("\n");
        }

        List<String> lines = queryLlama(sb.toString());
        List<TrackSearchQuery> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String line : lines) {
            parseLine(line).ifPresent(q -> {
                String key = q.getTrackName() + "|" + q.getArtistName();
                if (seen.add(key)) {
                    result.add(q);
                }
            });
            if (result.size() >= 50) break;
        }

        return result;
    }

    /**
     * Генерирует prompt для получения имён артистов.
     */
    public List<String> getArtistNames(String genre, String region, String mood) {
        String prompt = String.format(
                "Назови 20 %s%s%s исполнителей. Только имена. Без номеров, кавычек и пояснений.",
                mood != null ? mood + " " : "",
                genre != null ? genre + " " : "",
                region != null ? "из региона " + region : ""
        ).trim();

        System.out.println("[LlamaService] Prompt: " + prompt);

        List<String> lines = queryLlama(prompt);
        List<String> artists = new ArrayList<>();

        for (String line : lines) {
            String cleaned = line.replaceFirst("^\\d+[.)]?\\s*", "").trim();
            if (!cleaned.isBlank() && cleaned.length() < 80) {
                artists.add(cleaned);
            }
        }

        System.out.println("[LlamaService] Сгенерированные артисты: " + artists);
        return artists;
    }

    /**
     * Делает запрос к локальному API LLaMA и возвращает список строк.
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
     * Парсит строку формата "Артист, Название" (с удалением номера, дефиса и т.п.).
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
}
