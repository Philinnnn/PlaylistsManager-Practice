package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.services.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/callback")
    public ResponseEntity<String> handleSpotifyCallback(OAuth2AuthenticationToken authentication) {
        try {
            spotifyService.handleSpotifyCallback(authentication);
            return ResponseEntity.ok("Spotify аккаунт успешно привязан!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
