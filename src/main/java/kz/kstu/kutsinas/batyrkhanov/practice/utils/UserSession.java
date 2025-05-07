package kz.kstu.kutsinas.batyrkhanov.practice.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Getter
@Setter
@Component
@ToString(exclude = {"accessToken", "refreshToken"})
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class UserSession {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String displayName;
}