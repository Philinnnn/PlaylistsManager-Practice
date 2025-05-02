package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.User;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.UsersRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SpotifyController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private UsersRepo usersRepo;


    @GetMapping("/")
    public String home() {
        return "<a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
    }



    @GetMapping("/user")
    public Object user(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken() != null
                ? client.getRefreshToken().getTokenValue()
                : null;

        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

        UserSession session = UserSession.getInstance();
        session.setUserId((String) attributes.get("id"));
        session.setEmail((String) attributes.get("email"));
        session.setDisplayName((String) attributes.get("display_name"));
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);

        User user = new User(
                (String) attributes.get("id"),
                (String) attributes.get("display_name"),
                (String) attributes.get("email"),
                (String) attributes.get("country"),
                (String) attributes.get("product")
        );

        usersRepo.save(user);


        return attributes;
    }



}

