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
     * Registration of a new user.
     *
     * @param username user name
     * @param rawPassword password
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
     * Authentication of the user.
     *
     * @param username user name
     * @param password password
     * @param request http request
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
     * Проверка пароля и определение необходимости 2FA.
     * @return true если требуется 2FA, false если можно сразу логинить
     */
    public boolean loginUserWith2faCheck(String username, String password, HttpServletRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        AppUser user = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in DB"));
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            // Только аутентификация пароля, сессию не создаём
            return true;
        }
        // Если 2FA не требуется — обычный вход
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
        );
        refreshSpotifyAccessToken(user);
        loadUserSession(user);
        return false;
    }

    /**
     * Завершение входа после успешной 2FA
     */
    public void finishLoginAfter2fa(String username, HttpServletRequest request) {
        AppUser user = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in DB"));
        // Используем только username, не user.getPassword()!
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
        );
        refreshSpotifyAccessToken(user);
        loadUserSession(user);
    }

   /**
     * Updates Spotify access tokens for the user.
     *
     * @param user User object
     */
    public void refreshSpotifyAccessToken(AppUser user) {
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
     * Exit from the system.
     *
     * @param request http request
     */
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }

    /**
     * Downloads user data into the session.
     *
     * @param user User object
     */
    private void loadUserSession(AppUser user) {
        session.setUsername(user.getUsername());
        session.setUserId(String.valueOf(user.getId()));

        // Проверяем только наличие spotifyUserId
        if (user.getSpotifyUser() != null && user.getSpotifyUser().getId() != null) {
            session.setAccessToken("spotify_linked"); // Просто маркер
        } else {
            session.setAccessToken(null);
        }
    }

    /**
     * Checks if two-factor authentication is enabled for the user.
     *
     * @param username user name
     * @return true if 2FA is enabled, false otherwise
     */
    public boolean isTwoFactorEnabled(String username) {
        AppUser user = appUserRepo.findByUsername(username).orElse(null);
        return user != null && Boolean.TRUE.equals(user.getTwoFactorEnabled());
    }
}
