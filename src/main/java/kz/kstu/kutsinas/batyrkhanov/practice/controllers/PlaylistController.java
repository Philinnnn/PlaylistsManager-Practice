package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.dto.PlaylistRequest;
import kz.kstu.kutsinas.batyrkhanov.practice.services.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping("/create")
    public ResponseEntity<String> createPlaylist(@RequestBody PlaylistRequest request) {
        try {
            playlistService.createPlaylist(request);
            return ResponseEntity.ok("Playlist created!");
        } catch (Exception e) {
            System.err.println("Ошибка при создании плейлиста: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Ошибка при создании плейлиста");
        }
    }
}