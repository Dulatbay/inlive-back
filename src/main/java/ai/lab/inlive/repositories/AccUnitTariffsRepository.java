package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.AccUnitTariffs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccUnitTariffsRepository extends JpaRepository<AccUnitTariffs, Long> {
}

