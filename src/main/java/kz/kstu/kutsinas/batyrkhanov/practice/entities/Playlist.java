package kz.kstu.kutsinas.batyrkhanov.practice.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "playlists")
public class Playlist {
    @Id
    private String id;
    private String name;
    private String description;
    private Boolean isPublic;
    private String ownerId;


    public Playlist() {
    }

    public Playlist(String id, String name, Boolean isPublic, String description, String ownerId) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.description = description;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
