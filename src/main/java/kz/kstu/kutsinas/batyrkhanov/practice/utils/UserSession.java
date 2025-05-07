package kz.kstu.kutsinas.batyrkhanov.practice.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * Синглтон-класс для хранения информации о текущем пользователе Spotify в сессии.
 */
@Getter
@Setter
public class UserSession {

    private static final UserSession INSTANCE = new UserSession();

    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String displayName;

    private UserSession() {}

    public static UserSession getInstance() {
        return INSTANCE;
    }
}
