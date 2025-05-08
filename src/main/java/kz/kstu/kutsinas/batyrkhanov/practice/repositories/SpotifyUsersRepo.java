package kz.kstu.kutsinas.batyrkhanov.practice.repositories;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.SpotifyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpotifyUsersRepo extends JpaRepository<SpotifyUser,String> {
    @NonNull
    Optional<SpotifyUser> findById(String id);
    Optional<SpotifyUser> findByEmail(String email);
}
