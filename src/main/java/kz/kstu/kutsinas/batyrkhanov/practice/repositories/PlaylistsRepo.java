package kz.kstu.kutsinas.batyrkhanov.practice.repositories;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistsRepo extends JpaRepository<Playlist, String> {
}
