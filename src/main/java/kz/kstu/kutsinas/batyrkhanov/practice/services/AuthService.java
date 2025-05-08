package kz.kstu.kutsinas.batyrkhanov.practice.services;

import jakarta.servlet.http.HttpServletRequest;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepo appUserRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final UserSession session;

    /**
     * Регистрация нового пользователя.
     *
     * @param username     имя пользователя
     * @param rawPassword  пароль
     */
    public void registerUser(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new IllegalArgumentException("Username and password are required");
        }

        if (appUserRepo.findByUsername(username).isPresent()) {
            throw new IllegalStateException("User already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        appUserRepo.save(user);
    }

    /**
     * Аутентификация пользователя.
     *
     * @param username имя пользователя
     * @param password пароль
     * @param request  HTTP-запрос
     */
    public void loginUser(String username, String password, HttpServletRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
        );

        AppUser user = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in DB"));
        refreshSpotifyAccessToken(user);

        loadUserSession(user);
    }

    /**
     * Обновляет токен доступа Spotify для пользователя.
     *
     * @param user объект пользователя
     */
    private void refreshSpotifyAccessToken(AppUser user) {
        var spotifyUser = user.getSpotifyUser();
        if (spotifyUser == null || spotifyUser.getRefreshToken() == null) return;

        try {
            var spotifyApi = new SpotifyApi.Builder()
                    .setClientId(System.getProperty("SPOTIFY_CLIENT_ID"))
                    .setClientSecret(System.getProperty("SPOTIFY_CLIENT_SECRET"))
                    .build();

            var credentials = spotifyApi
                    .authorizationCodeRefresh()
                    .refresh_token(spotifyUser.getRefreshToken())
                    .build()
                    .execute();

            spotifyUser.setAccessToken(credentials.getAccessToken());
            appUserRepo.save(user);

        } catch (Exception e) {
            System.err.println("Не удалось обновить токен: " + e.getMessage());
        }
    }

    /**
     * Выход из системы.
     *
     * @param request HTTP-запрос
     */
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }

    /**
     * Загружает данные пользователя в сессию.
     *
     * @param user объект пользователя
     */
    private void loadUserSession(AppUser user) {
        session.setUsername(user.getUsername());
        session.setUserId(String.valueOf(user.getId()));

        if (user.getSpotifyUser() != null) {
            var spotify = user.getSpotifyUser();
            session.setEmail(spotify.getEmail());
            session.setDisplayName(spotify.getDisplayName());
            session.setAccessToken(spotify.getAccessToken());
            session.setRefreshToken(spotify.getRefreshToken());
        } else {
            session.setEmail("local user@" + user.getUsername());
            session.setDisplayName(user.getUsername());
        }
    }
}
