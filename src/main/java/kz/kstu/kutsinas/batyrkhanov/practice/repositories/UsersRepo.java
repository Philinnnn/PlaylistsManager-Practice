package kz.kstu.kutsinas.batyrkhanov.practice.repositories;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<User,String> {
    @NonNull
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
}
