package kz.kstu.kutsinas.batyrkhanov.practice;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.fail;

public class YouTubeIntegrationTest {

    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/youtube.readonly");

    @Test
    public void testYouTubeOAuth2AuthorizationFlow() throws Exception {
        Map<String, String> env = loadEnv();
        String clientId = env.get("GOOGLE_CLIENT_ID");
        String clientSecret = env.get("GOOGLE_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            fail("GOOGLE_CLIENT_ID или GOOGLE_CLIENT_SECRET отсутствует в .env");
        }

        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientId, clientSecret, SCOPES
        ).setAccessType("offline").build();

        String redirectUri = "urn:ietf:wg:oauth:2.0:oob";

        String authUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();

        System.out.println("\n🔗 Открой в браузере эту ссылку и авторизуйся:");
        System.out.println(authUrl);
        System.out.print(" Вставь код авторизации здесь: ");

        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();

        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
        GoogleCredential credential = new GoogleCredential().setFromTokenResponse(tokenResponse);

        YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("YouTubeOAuthTestApp")
                .build();

        YouTube.Search.List search = youtube.search()
                .list("snippet")
                .setQ("Spring Boot tutorial")
                .setType("video")
                .setMaxResults(5L);

        SearchListResponse response = search.execute();
        List<SearchResult> items = response.getItems();

        assertNotNull(items);
        assertFalse(items.isEmpty());

        System.out.println("✅ Найдено видео: " + items.size());
        for (SearchResult item : items) {
            System.out.println("- " + item.getSnippet().getTitle());
        }
    }

    private Map<String, String> loadEnv() {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/.env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    String[] pair = line.split("=", 2);
                    env.put(pair[0].trim(), pair[1].trim());
                }
            }
        } catch (Exception e) {
            fail("Ошибка чтения .env файла: " + e.getMessage());
        }
        return env;
    }


}
