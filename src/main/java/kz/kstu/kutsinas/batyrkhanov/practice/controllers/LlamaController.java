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
            List<TrackSearchQuery> trackPool = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            for (String artist : artistNames) {
                List<TrackSearchQuery> topTracksByArtist = spotifyService.getTopTracksByArtist(artist, region);
                for (TrackSearchQuery track : topTracksByArtist) {
                    String key = track.getTrackName() + "|" + track.getArtistName();
                    if (seen.add(key)) {
                        trackPool.add(track);
                    }
                }
            }

            List<TrackSearchQuery> finalList = llamaService.selectBestTracks(trackPool, genre, region, mood);
            return ResponseEntity.ok(finalList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/generate-by-top-tracks")
    public ResponseEntity<List<TrackSearchQuery>> generateFromTopTracks() {
        try {
            List<TrackSearchQuery> topTracks = spotifyService.getUserTopTracks();

            Set<String> uniqueArtists = new HashSet<>();
            for (TrackSearchQuery track : topTracks) {
                if (uniqueArtists.size() >= 20) break;
                uniqueArtists.add(track.getArtistName());
            }

            List<TrackSearchQuery> trackPool = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (String artist : uniqueArtists) {
                List<TrackSearchQuery> artistTracks = spotifyService.getTopTracksByArtist(artist, null);
                for (TrackSearchQuery track : artistTracks) {
                    String key = track.getTrackName() + "|" + track.getArtistName();
                    if (seen.add(key)) {
                        trackPool.add(track);
                    }
                }
            }

            List<TrackSearchQuery> finalList = llamaService.selectBestTracks(topTracks, trackPool);
            return ResponseEntity.ok(finalList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
