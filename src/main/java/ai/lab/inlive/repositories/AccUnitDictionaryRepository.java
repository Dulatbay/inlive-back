package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.AccUnitDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccUnitDictionaryRepository extends JpaRepository<AccUnitDictionary, Long> {
}

