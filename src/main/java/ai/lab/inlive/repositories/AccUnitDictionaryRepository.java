package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.AccUnitDictionary;
import ai.lab.inlive.entities.AccommodationUnit;
import ai.lab.inlive.entities.enums.DictionaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccUnitDictionaryRepository extends JpaRepository<AccUnitDictionary, Long> {

    @Query("SELECT aud FROM AccUnitDictionary aud WHERE aud.unit = :unit AND aud.dictionary.key = :key")
    List<AccUnitDictionary> findByUnitAndDictionaryKey(@Param("unit") AccommodationUnit unit, @Param("key") DictionaryKey key);

    @Modifying
    @Query("DELETE FROM AccUnitDictionary aud WHERE aud.unit = :unit AND aud.dictionary.key = :key")
    void deleteByUnitAndDictionaryKey(@Param("unit") AccommodationUnit unit, @Param("key") DictionaryKey key);
}
