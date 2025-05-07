package kz.kstu.kutsinas.batyrkhanov.practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация Spring Security:
 * - Разрешает доступ к корневому маршруту и странице входа без авторизации
 * - Все остальные запросы требуют аутентификации
 * - Используется OAuth2-вход с кастомной страницей /login
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                )
                .build();
    }
}
