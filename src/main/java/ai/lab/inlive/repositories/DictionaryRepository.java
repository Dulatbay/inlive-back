package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.Dictionary;
import ai.lab.inlive.entities.enums.DictionaryKey;
import jakarta.validation.constraints.NotNull;
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

    @Query(value = """
        SELECT d.*
        FROM dictionaries d
        WHERE TRUE
          AND (CAST(:isDeleted AS boolean) IS NULL OR d.is_deleted = CAST(:isDeleted AS boolean))
          AND (
                CAST(:keysProvided AS boolean) = FALSE
                OR EXISTS (
                    SELECT 1
                    FROM unnest(CAST(:keys AS text[])) AS k
                    WHERE UPPER(d.key::text) = UPPER(k)   -- строгий матч по ключу
                )
              )
          AND (CAST(:value AS text) IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || CAST(:value AS text) || '%'))
        ORDER BY d.id ASC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM dictionaries d
        WHERE TRUE
          AND (CAST(:isDeleted AS boolean) IS NULL OR d.is_deleted = CAST(:isDeleted AS boolean))
          AND (
                CAST(:keysProvided AS boolean) = FALSE
                OR EXISTS (
                    SELECT 1
                    FROM unnest(CAST(:keys AS text[])) AS k
                    WHERE UPPER(d.key::text) = UPPER(k)
                )
              )
          AND (CAST(:value AS text) IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || CAST(:value AS text) || '%'))
        """,
        nativeQuery = true)
    Page<Dictionary> findWithFilters(
            @Param("isDeleted") Boolean isDeleted,
            @Param("keys") String[] keys,
            @Param("keysProvided") boolean keysProvided,
            @Param("value") String value,
            Pageable pageable
    );

    boolean existsByKeyAndIsDeletedFalse(@NotNull DictionaryKey key);
}