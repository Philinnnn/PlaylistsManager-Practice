package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Представляет пользователя приложения, сохраняемого в БД.
 */
@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_user_id", referencedColumnName = "id")
    private SpotifyUser spotifyUser;
}
