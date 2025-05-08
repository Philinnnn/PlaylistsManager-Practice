package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.*;
import lombok.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_public")
    private Boolean isPublic;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Transient
    private List<Track> tracks = new ArrayList<>();

    public void addTrack(Track track) {
        tracks.add(track);
    }
}
