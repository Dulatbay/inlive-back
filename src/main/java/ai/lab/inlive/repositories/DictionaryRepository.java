package ai.lab.inlive.repositories;

import ai.lab.inlive.dto.params.DictionarySearchParams;
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
            WHERE (:#{#params.isDeleted} IS NULL OR d.is_deleted = :#{#params.isDeleted})
              AND (
                   :#{#params.keys == null || #params.keys.isEmpty()} = TRUE
                   OR d."key" IN (:#{#params.keys.![name()]})
              )
              AND (:#{#params.value} IS NULL OR UPPER(d.value) LIKE UPPER(CONCAT('%', :#{#params.value}, '%')))
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM dictionaries d
                    WHERE (:#{#params.isDeleted} IS NULL OR d.is_deleted = :#{#params.isDeleted})
                      AND (
                           :#{#params.keys == null || #params.keys.isEmpty()} = TRUE
                           OR d."key" IN (:#{#params.keys.![name()]})
                      )
                      AND (:#{#params.value} IS NULL OR UPPER(d.value) LIKE UPPER(CONCAT('%', :#{#params.value}, '%')))
                    """,
            nativeQuery = true)
    Page<Dictionary> findWithFilters(@Param("params") DictionarySearchParams params, Pageable pageable);

    boolean existsByKeyAndIsDeletedFalse(@NotNull DictionaryKey key);
}