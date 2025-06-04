package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name ="google_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUser {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "access")
    private String accessToken;

    @Column(name = "refresh")
    private String refreshToken;

    @OneToOne(mappedBy = "googleUser")
    private AppUser appUser;
}
