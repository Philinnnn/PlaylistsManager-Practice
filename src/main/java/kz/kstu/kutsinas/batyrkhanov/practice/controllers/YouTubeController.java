package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.YouTubeVideo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {
    private static final String API_KEY = System.getenv("YOUTUBE_API_KEY");

    @GetMapping("/search")
    public ResponseEntity<?> searchYouTube(@RequestParam String q) {
        try {
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null
            ).setApplicationName("MyApp").build();

            YouTube.Search.List search = youtubeService.search()
                    .list("snippet")
                    .setQ(q)
                    .setType("video")
                    .setMaxResults(5L)
                    .setKey(API_KEY);

            SearchListResponse response = search.execute();

            List<YouTubeVideo> result = response.getItems().stream()
                    .map(item -> {
                        String videoId = item.getId().getVideoId();
                        String title = item.getSnippet().getTitle();
                        String description = item.getSnippet().getDescription();
                        String thumbnail = item.getSnippet().getThumbnails().getDefault().getUrl();
                        return new YouTubeVideo(videoId, title, description, thumbnail);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("Error when contacting YouTube API: " + e.getMessage());
        }
    }
}
