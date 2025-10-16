package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.Dictionary;
import ai.lab.inlive.entities.enums.DictionaryKey;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    Optional<Dictionary> findByIdAndIsDeletedFalse(Long id);

    @Query(value = "SELECT * FROM dictionaries d WHERE TRUE " +
           "AND (:isDeleted IS NULL OR d.is_deleted = :isDeleted) " +
           "AND (:keys IS NULL OR array_length(CAST(:keys AS text[]), 1) IS NULL OR " +
           "     EXISTS (SELECT 1 FROM unnest(CAST(:keys AS text[])) AS key_item " +
           "             WHERE UPPER(d.key::text) LIKE UPPER('%' || key_item || '%'))) " +
           "AND (:value IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || :value || '%'))",
           countQuery = "SELECT COUNT(*) FROM dictionaries d WHERE TRUE " +
           "AND (:isDeleted IS NULL OR d.is_deleted = :isDeleted) " +
           "AND (:keys IS NULL OR array_length(CAST(:keys AS text[]), 1) IS NULL OR " +
           "     EXISTS (SELECT 1 FROM unnest(CAST(:keys AS text[])) AS key_item " +
           "             WHERE UPPER(d.key::text) LIKE UPPER('%' || key_item || '%'))) " +
           "AND (:value IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || :value || '%'))",
           nativeQuery = true)
    Page<Dictionary> findWithFilters(
            @Param("isDeleted") Boolean isDeleted,
            @Param("keys") String[] keys,
            @Param("value") String value,
            Pageable pageable
    );

    boolean existsByKeyAndIsDeletedFalse(@NotBlank DictionaryKey key);
}
