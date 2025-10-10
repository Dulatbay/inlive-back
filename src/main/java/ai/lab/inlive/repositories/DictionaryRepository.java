package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.Dictionary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    Optional<Dictionary> findByIdAndIsDeletedFalse(Long id);

    List<Dictionary> findAllByIsDeletedFalse();

    Page<Dictionary> findAllByIsDeletedFalse(Pageable pageable);

    List<Dictionary> findByTypeAndIsDeletedFalse(String type);

    @Query(value = "SELECT * FROM dictionaries d WHERE 1=1 " +
           "AND (:type IS NULL OR d.type = :type) " +
           "AND (:isDeleted IS NULL OR d.is_deleted = :isDeleted) " +
           "AND (:key IS NULL OR UPPER(d.key::text) LIKE UPPER('%' || :key || '%')) " +
           "AND (:value IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || :value || '%'))",
           countQuery = "SELECT COUNT(*) FROM dictionaries d WHERE 1=1 " +
           "AND (:type IS NULL OR d.type = :type) " +
           "AND (:isDeleted IS NULL OR d.is_deleted = :isDeleted) " +
           "AND (:key IS NULL OR UPPER(d.key::text) LIKE UPPER('%' || :key || '%')) " +
           "AND (:value IS NULL OR UPPER(d.value::text) LIKE UPPER('%' || :value || '%'))",
           nativeQuery = true)
    Page<Dictionary> findWithFilters(
            @Param("type") String type,
            @Param("isDeleted") Boolean isDeleted,
            @Param("key") String key,
            @Param("value") String value,
            Pageable pageable
    );

    boolean existsByKeyAndTypeAndIsDeletedFalse(String key, String type);
}
