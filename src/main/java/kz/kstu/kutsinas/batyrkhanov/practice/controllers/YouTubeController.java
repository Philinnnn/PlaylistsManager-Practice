package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.YouTubeVideo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.PropertyLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YouTubeController {

    private static   final  Map<String, String> env= PropertyLoader.loadEnv();
    private static final String API_KEY = env.get("GOOGLE_API_KEY");

    @GetMapping("/search")
    public ResponseEntity<?> searchYouTube(@RequestParam String q) {
        try {
            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    request -> {}
            ).setApplicationName("MyApp").build();

            YouTube.Search.List search = youtube.search()
                    .list("snippet")
                    .setQ(q)
                    .setType("video")
                    .setMaxResults(5L)
                    .setKey(API_KEY);

            SearchListResponse searchResponse = search.execute();

            List<YouTubeVideo> result = searchResponse.getItems().stream()
                    .map(item -> new YouTubeVideo(
                            item.getId().getVideoId(),
                            item.getSnippet().getTitle(),
                            item.getSnippet().getDescription(),
                            item.getSnippet().getThumbnails().getDefault().getUrl()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("YouTube API error: " + e.getMessage());
        }
    }
}


