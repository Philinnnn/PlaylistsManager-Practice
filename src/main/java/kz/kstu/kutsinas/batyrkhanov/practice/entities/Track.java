package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Представляет трек Spotify, сохраняемый в БД.
 */
@Entity
@Table(name = "tracks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "duration_ms")
    private int durationMs;

    @Column(name = "album_name")
    private String albumName;

    @Column(name = "artist_names")
    private String artistNames;

    @Column(name = "uri")
    private String uri;
}
