package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.dto.TrackSearchQuery;
import kz.kstu.kutsinas.batyrkhanov.practice.services.LlamaService;
import kz.kstu.kutsinas.batyrkhanov.practice.services.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/sound-pick")
@RequiredArgsConstructor
public class SoundPickController {

    private final SpotifyService spotifyService;
    private final LlamaService llamaService;

    /**
     * Voting processing.
     */
    @PostMapping("/vote")
    @ResponseBody
    public ResponseEntity<?> vote(@RequestBody Map<String, String> payload) {
        String trackName = payload.get("trackName");
        String artistName = payload.get("artistName");
        String vote = payload.get("vote");

        System.out.println("🎧 Голос: " + trackName + " — " + artistName + " → " + vote);
        return ResponseEntity.ok().build();
    }

    /**
     * SoundPick: Get recommendations with a preview.
     */
    @GetMapping("/tracks")
    public ResponseEntity<List<Map<String, Object>>> getSoundPickTracks() {
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

            List<Map<String, Object>> response = finalList.stream()
                    .map(track -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("trackName", track.getTrackName());
                        map.put("artistName", track.getArtistName());
                        map.put("spotifyId", spotifyService.getTrackId(track.getArtistName(), track.getTrackName()));
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
