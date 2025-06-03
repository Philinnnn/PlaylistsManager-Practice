package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/soundtrack")
@RequiredArgsConstructor
public class SoundtrackController {

    @GetMapping("/search")
    public ResponseEntity<List<String>> generateImdbSearchUrl(
            @RequestParam String track,
            @RequestParam String artist) {

        System.out.println("[SoundtrackController] Входящий запрос: " + track + " — " + artist);

        String query = String.format("site:imdb.com/title inurl:soundtrack \"%s\" \"%s\"",
                track, artist);
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String googleUrl = "https://www.google.com/search?q=" + encodedQuery;

        return ResponseEntity.ok(List.of(googleUrl));
    }
}
