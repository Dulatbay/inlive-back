package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.AccDictionary;
import ai.lab.inlive.entities.Accommodation;
import ai.lab.inlive.entities.enums.DictionaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccDictionaryRepository extends JpaRepository<AccDictionary, Long> {
    @Modifying
    @Query("DELETE FROM AccDictionary ad WHERE ad.accommodation = :accommodation AND ad.dictionary.key = :key")
    void deleteByAccommodationAndDictionaryKey(@Param("accommodation") Accommodation accommodation, @Param("key") DictionaryKey key);
}

