package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.services.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/callback")
    public RedirectView handleSpotifyCallback(OAuth2AuthenticationToken authentication) {
        try {
            spotifyService.handleSpotifyCallback(authentication);
            return new RedirectView("/dashboard");
        } catch (RuntimeException e) {
            return new RedirectView("/error?message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
