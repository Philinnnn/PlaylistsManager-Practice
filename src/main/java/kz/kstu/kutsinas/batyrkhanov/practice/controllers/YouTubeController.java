package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import jakarta.servlet.http.HttpServletResponse;
import kz.kstu.kutsinas.batyrkhanov.practice.config.GoogleOAuthConfig;
import kz.kstu.kutsinas.batyrkhanov.practice.dto.YouTubeVideo;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.GoogleUser;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.GoogleUsersRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YouTubeController {

    private final UserSession session;
    private final GoogleOAuthConfig config;
    private final GoogleUsersRepo userRepo;

    @GetMapping("/search")
    public ResponseEntity<?> searchYouTube(@RequestParam String q, HttpServletResponse response) throws IOException {

        if (session.getYoutubeAccessToken() == null) {
            String oauthUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=" + config.getClientId() +
                    "&redirect_uri=" + config.getRedirectUri() +
                    "&response_type=code" +
                    "&scope=https://www.googleapis.com/auth/youtube.readonly%20openid%20email%20profile" +
                    "&access_type=offline" +
                    "&prompt=consent";

            return ResponseEntity.status(302).header("Location", oauthUrl).build();
        }

        try {
            // 2. YouTube API запрос с access_token
            Credential credential = new GoogleCredential().setAccessToken(session.getYoutubeAccessToken());

            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("MyApp").build();

            YouTube.Search.List search = youtube.search()
                    .list("snippet")
                    .setQ(q)
                    .setType("video")
                    .setMaxResults(5L);

            SearchListResponse searchResponse = search.execute();

            List<YouTubeVideo> result = searchResponse.getItems().stream()
                    .map(item -> new YouTubeVideo(
                            item.getId().getVideoId(),
                            item.getSnippet().getTitle(),
                            item.getSnippet().getDescription(),
                            item.getSnippet().getThumbnails().getDefault().getUrl()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("YouTube API error: " + e.getMessage());
        }
    }

    @GetMapping("/oauth2callback")
    public ResponseEntity<String> oauthCallback(@RequestParam String code) {
        try {
            // Обмен кода на токены
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    config.getClientId(),
                    config.getClientSecret(),
                    code,
                    config.getRedirectUri()
            ).execute();

            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();

            // Получение профиля пользователя
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

            Oauth2 oauth2 = new Oauth2.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("MyApp").build();

//            Userinfoplus userInfo = oauth2.userinfo().get().execute();
//
//            // Сохраняем в session
//            session.setYoutubeAccessToken(accessToken);
//            session.setYoutubeRefreshToken(refreshToken);
//            session.setEmail(userInfo.getEmail());
//            session.setDisplayName(userInfo.getName());
//            session.setUserId(userInfo.getId());
//
//            // Сохраняем в БД, если ещё не сохранён
//            if (!userRepo.existsById(userInfo.getId())) {
//                GoogleUser user = new GoogleUser(
//                        userInfo.getId(),
//                        accessToken,
//                        refreshToken,
//                        null
//                );
//                userRepo.save(user);
//            }

            // Редирект обратно на YouTube-поиск
            return ResponseEntity.status(302).header("Location", "/api/youtube/search?q=Spring").build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("OAuth callback error: " + e.getMessage());
        }
    }
}

