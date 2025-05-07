package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.User;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.UsersRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Контроллер, обрабатывающий аутентификацию через Spotify.
 */
@RestController
public class SpotifyController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UsersRepo usersRepo;

    @Autowired
    public SpotifyController(OAuth2AuthorizedClientService authorizedClientService, UsersRepo usersRepo) {
        this.authorizedClientService = authorizedClientService;
        this.usersRepo = usersRepo;
    }

    @GetMapping("/")
    public String home() {
        return "<a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
    }

    /**
     * Обработка данных пользователя после авторизации через Spotify.
     */
    @GetMapping("/user")
    public Map<String, Object> user(OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

        String accessToken = getAccessToken(authentication);
        String refreshToken = getRefreshToken(authentication);

        saveToSession(attributes, accessToken, refreshToken);
        saveToDatabase(attributes);

        return attributes;
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

    private void saveToSession(Map<String, Object> attributes, String accessToken, String refreshToken) {
        UserSession session = UserSession.getInstance();
        session.setUserId((String) attributes.get("id"));
        session.setEmail((String) attributes.get("email"));
        session.setDisplayName((String) attributes.get("display_name"));
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
    }

    private void saveToDatabase(Map<String, Object> attributes) {
        User user = new User(
                (String) attributes.get("id"),
                (String) attributes.get("display_name"),
                (String) attributes.get("email"),
                (String) attributes.get("country"),
                (String) attributes.get("product")
        );
        usersRepo.save(user);
    }
}
