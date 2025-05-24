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

    private final SpotifyService spotifyService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${LLAMA_HOST:http://localhost:11434}")
    private String llamaHost;

    @Value("${LLAMA_MODEL:llama3}")
    private String llamaModel;

    /**
     * Формирует prompt в стиле "Составь список треков..." и парсит его в список треков.
     */
    public List<TrackSearchQuery> getTrackRecommendations(String genre, String region, String mood) {
        String prompt = buildPrompt(genre, region, mood);
        System.out.println("[LlamaService] Prompt: " + prompt);

        List<String> lines = queryLlama(prompt);
        List<TrackSearchQuery> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String line : lines) {
            parseLine(line).ifPresent(q -> {
                String key = q.getTrackName() + "|" + q.getArtistName();
                if (seen.add(key)) {
                    spotifyService.searchTrack(q.getTrackName(), q.getArtistName())
                            .ifPresent(found -> result.add(new TrackSearchQuery(found.getName(), found.getArtists()[0].getName())));
                }
            });
            if (result.size() >= 50) break;
        }

        return result;
    }

    /**
     * Генерирует prompt для списка треков.
     */
    public String buildPrompt(String genre, String region, String mood) {
        StringBuilder sb = new StringBuilder("Составь список из 20 ");
        if (mood != null && !mood.isBlank()) sb.append(mood).append(" ");
        if (genre != null && !genre.isBlank()) sb.append(genre).append(" ");
        sb.append("треков");
        if (region != null && !region.isBlank()) sb.append(" из региона ").append(region);
        sb.append(". Формат строго такой: Артист, Название (никаких номеров, кавычек, пояснений, переносов строк в названии, ничего лишнего). Ответ только списком. Без текста до и после.");
        return sb.toString();
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
