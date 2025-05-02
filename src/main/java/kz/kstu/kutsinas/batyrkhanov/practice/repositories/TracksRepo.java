package kz.kstu.kutsinas.batyrkhanov.practice.repositories;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TracksRepo extends JpaRepository<Track, String> {

}
