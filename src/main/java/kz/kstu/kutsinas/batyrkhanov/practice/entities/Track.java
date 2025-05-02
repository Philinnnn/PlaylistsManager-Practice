package kz.kstu.kutsinas.batyrkhanov.practice.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tracks")
public class Track {
    @Id
    private String id;
    private String name;
    private int durationMs;
    private String albumName;
    private String artistNames;
    private String uri;

    public Track() {
    }

    public Track(String id, String name, int durationMs, String albumName, String artistNames, String uri) {
        this.id = id;
        this.name = name;
        this.durationMs = durationMs;
        this.albumName = albumName;
        this.artistNames = artistNames;
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistNames() {
        return artistNames;
    }

    public String getUri() {
        return uri;
    }
}
