package kz.kstu.kutsinas.batyrkhanov.practice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String displayName;
    private String email;
    private String country;
    private String product;

    public User() {
    }

    public User(String id, String displayName, String email, String country, String product) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.country = country;
        this.product = product;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getCountry() {
        return country;
    }

    public String getProduct() {
        return product;
    }
}
