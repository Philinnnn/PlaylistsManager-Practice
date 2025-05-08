package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

/**
 * Представляет пользователя Spotify, сохраняемого в БД.
 */
@Entity
@Table(name = "spotify_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyUser {

    public SpotifyUser(Map<String, Object> attributes, String accessToken, String refreshToken)
    {
        this.id = (String) attributes.get("id");
        this.displayName = (String) attributes.get("display_name");
        this.email = (String) attributes.get("email");
        this.country = (String) attributes.get("country");
        this.product = (String) attributes.get("product");
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "email")
    private String email;

    @Column(name = "country")
    private String country;

    @Column(name = "product")
    private String product;

    @Column(name = "access_token", length = 512)
    private String accessToken;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @OneToOne(mappedBy = "spotifyUser")
    private AppUser appUser;
}
