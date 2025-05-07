package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Представляет пользователя Spotify, сохраняемого в БД.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

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
}
