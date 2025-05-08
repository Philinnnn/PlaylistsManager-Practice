package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.User;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.UsersRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final AppUserRepo appUserRepo;
    private final UsersRepo usersRepo;
    private final UserSession session;

    @Autowired
    public SpotifyController(OAuth2AuthorizedClientService authorizedClientService,
                             AppUserRepo appUserRepo,
                             UsersRepo usersRepo,
                             UserSession session) {
        this.authorizedClientService = authorizedClientService;
        this.appUserRepo = appUserRepo;
        this.usersRepo = usersRepo;
        this.session = session;
    }

    /**
     * Метод вызывается после успешной авторизации через Spotify
     */
    @GetMapping("/callback")
    public ResponseEntity<String> handleSpotifyCallback(OAuth2AuthenticationToken authentication) {
        // Проверка: пользователь приложения вошёл?
        String appUsername = session.getUsername();
        if (appUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to link Spotify account.");
        }

        // Получаем Spotify-данные
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String accessToken = getAccessToken(authentication);
        String refreshToken = getRefreshToken(authentication);

        // Создаём объект Spotify-пользователя
        User spotifyUser = new User(
                (String) attributes.get("id"),
                (String) attributes.get("display_name"),
                (String) attributes.get("email"),
                (String) attributes.get("country"),
                (String) attributes.get("product"),
                null // appUser будет установлен позже
        );

        usersRepo.save(spotifyUser);

        // Находим текущего пользователя приложения и связываем
        AppUser appUser = appUserRepo.findByUsername(appUsername)
                .orElseThrow(() -> new RuntimeException("AppUser not found"));

        appUser.setSpotifyUser(spotifyUser);
        appUserRepo.save(appUser);

        // Сохраняем сессионные данные
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setUserId(spotifyUser.getId());
        session.setEmail(spotifyUser.getEmail());
        session.setDisplayName(spotifyUser.getDisplayName());

        return ResponseEntity.ok("Spotify аккаунт успешно привязан!");
    }

    private String getAccessToken(OAuth2AuthenticationToken auth) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                auth.getAuthorizedClientRegistrationId(), auth.getName());
        return client.getAccessToken().getTokenValue();
    }

    private String getRefreshToken(OAuth2AuthenticationToken auth) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                auth.getAuthorizedClientRegistrationId(), auth.getName());
        return client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;
    }


}
