package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakId(String id);

    boolean existsByPhoneNumber(String phoneNumber);
}