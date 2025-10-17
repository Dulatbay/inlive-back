package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.AccommodationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccommodationUnitRepository extends JpaRepository<AccommodationUnit, Long> {
    Optional<AccommodationUnit> findByIdAndIsDeletedFalse(Long id);
}
