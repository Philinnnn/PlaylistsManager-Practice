package kz.kstu.kutsinas.batyrkhanov.practice.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}