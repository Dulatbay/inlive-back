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

    @Query(
        value = """
            SELECT d.*
            FROM dictionaries d
            WHERE TRUE
              -- isDeleted: либо не фильтруем (NULL), либо точное сравнение
              AND (CAST(:isDeleted AS boolean) IS NULL OR d.is_deleted = CAST(:isDeleted AS boolean))

              -- keys: если NULL или пустой массив — не фильтруем; иначе точное равенство по ключу
              AND (
                    COALESCE(cardinality(CAST(:keys AS text[])), 0) = 0
                    OR EXISTS (
                        SELECT 1
                        FROM unnest(CAST(:keys AS text[])) AS k
                        WHERE UPPER(d.key::text) = UPPER(k)
                    )
                  )

              -- value: если NULL — не фильтруем; иначе LIKE по value (case-insensitive)
              AND (CAST(:value AS text) IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || CAST(:value AS text) || '%'))

            ORDER BY d.id ASC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM dictionaries d
            WHERE TRUE
              AND (CAST(:isDeleted AS boolean) IS NULL OR d.is_deleted = CAST(:isDeleted AS boolean))
              AND (
                    COALESCE(cardinality(CAST(:keys AS text[])), 0) = 0
                    OR EXISTS (
                        SELECT 1
                        FROM unnest(CAST(:keys AS text[])) AS k
                        WHERE UPPER(d.key::text) = UPPER(k)
                    )
                  )
              AND (CAST(:value AS text) IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || CAST(:value AS text) || '%'))
            """,
        nativeQuery = true
    )
    Page<Dictionary> findWithFilters(
        @Param("isDeleted") Boolean isDeleted,   // null => не фильтруем
        @Param("keys") String[] keys,            // null/[] => не фильтруем; иначе точное равенство по key
        @Param("value") String value,            // null => не фильтруем; иначе LIKE по value
        Pageable pageable
    );

    boolean existsByKeyAndIsDeletedFalse(@NotNull DictionaryKey key);
}