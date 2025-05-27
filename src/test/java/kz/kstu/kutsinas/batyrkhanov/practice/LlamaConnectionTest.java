package kz.kstu.kutsinas.batyrkhanov.practice;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LlamaConnectionTest {

    private Map<String, String> loadEnv() {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/.env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        env.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            fail("Не удалось прочитать .env файл: " + e.getMessage());
        }
        return env;
    }

    @Test
    public void testLlamaConnection() {
        Map<String, String> env = loadEnv();
        String llamaHost = env.get("LLAMA_HOST");
        String llamaModel = env.get("LLAMA_MODEL");

        assertNotNull(llamaHost, "LLAMA_HOST не задан в .env");
        assertNotNull(llamaModel, "LLAMA_MODEL не задан в .env");

        String url = llamaHost + "/api/generate";

        Map<String, Object> requestBody = Map.of(
                "model", llamaModel,
                "prompt", "Назови 3 казахстанских исполнителя. Только список.",
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "LLaMA должна вернуть 200 OK");
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isBlank(), "Ответ LLaMA не должен быть пустым");

        System.out.println("✅ Ответ от LLaMA:\n" + response.getBody());
    }
}
