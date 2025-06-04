package kz.kstu.kutsinas.batyrkhanov.practice.repositories;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.GoogleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleUsersRepo extends JpaRepository<GoogleUser, Long> {
}
