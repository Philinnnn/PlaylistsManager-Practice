package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import kz.kstu.kutsinas.batyrkhanov.practice.services.LastFmService;
import kz.kstu.kutsinas.batyrkhanov.practice.services.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;

@RestController
@RequestMapping("/lastfm")
@RequiredArgsConstructor
public class LastFmController {

    private final SpotifyService spotifyService;
    private final LastFmService lastFmService;

    /**
     * Генерирует список рекомендаций от Last.fm, используя случайные треки пользователя.
     */
    @GetMapping("/generate-recommendations")
    public ResponseEntity<List<TrackSearchQuery>> generateFromTopTracks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String mood
    ) {
        try {
            List<Track> topTracks = spotifyService.getRandomTracks(genre, region, mood);
            List<TrackSearchQuery> result = lastFmService.getRecommendations(topTracks, genre, region, mood);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
