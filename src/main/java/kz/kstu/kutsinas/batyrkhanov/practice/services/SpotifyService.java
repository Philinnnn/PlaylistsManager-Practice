package kz.kstu.kutsinas.batyrkhanov.practice.services;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.SpotifyUser;
import kz.kstu.kutsinas.batyrkhanov.practice.enums.TokenType;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.SpotifyUsersRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final AppUserRepo appUserRepo;
    private final SpotifyUsersRepo usersRepo;
    private final UserSession session;

    /**
     * Устанавливает связь между пользователем приложения и его аккаунтом Spotify.
     * @param authentication OAuth2AuthenticationToken, полученный от Spring Security после успешной аутентификации.
     */
    public void handleSpotifyCallback(OAuth2AuthenticationToken authentication) {
        String appUsername = session.getUsername();
        if (appUsername == null) {
            throw new RuntimeException("You must be logged in to link Spotify account.");
        }

        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

        String accessToken = getToken(authentication, TokenType.ACCESS);
        String refreshToken = getToken(authentication, TokenType.REFRESH);

        SpotifyUser spotifyUser = new SpotifyUser(attributes, accessToken, refreshToken);
        usersRepo.save(spotifyUser);

        AppUser appUser = appUserRepo.findByUsername(appUsername)
                .orElseThrow(() -> new RuntimeException("AppUser not found"));
        appUser.setSpotifyUser(spotifyUser);
        spotifyUser.setAppUser(appUser);
        appUserRepo.save(appUser);

        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);

        System.out.println("Session: " + session);
    }

    /**
     * Получает токен доступа или обновления для указанного типа.
     * @param auth OAuth2AuthenticationToken, полученный от Spring Security после успешной аутентификации.
     * @param type Тип токена (ACCESS или REFRESH).
     * @return Значение токена.
     */
    private String getToken(OAuth2AuthenticationToken auth, TokenType type) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                auth.getAuthorizedClientRegistrationId(), auth.getName()
        );

        return switch (type) {
            case ACCESS -> client.getAccessToken().getTokenValue();
            case REFRESH -> client.getRefreshToken() != null
                    ? client.getRefreshToken().getTokenValue()
                    : null;
        };
    }
}