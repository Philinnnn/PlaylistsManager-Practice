package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import kz.kstu.kutsinas.batyrkhanov.practice.services.LlamaService;
import kz.kstu.kutsinas.batyrkhanov.practice.services.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/llama")
@RequiredArgsConstructor
public class LlamaController {

    private final SpotifyService spotifyService;
    private final LlamaService llamaService;

    /**
     * Генерирует список рекомендаций от LLaMA, используя жанр, настроение и регион.
     */
    @GetMapping("/generate-recommendations")
    public ResponseEntity<List<TrackSearchQuery>> generateFromArtistList(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String mood
    ) {
        try {
            List<String> artistNames = llamaService.getArtistNames(genre, region, mood);

            List<TrackSearchQuery> result = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            for (String artist : artistNames) {
                spotifyService.getRandomTrackByArtist(artist).ifPresent(track -> {
                    String key = track.getTrackName() + "|" + track.getArtistName();
                    if (seen.add(key)) {
                        result.add(track);
                    }
                });
                if (result.size() >= 50) break;
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
