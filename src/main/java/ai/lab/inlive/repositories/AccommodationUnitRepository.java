package ai.lab.inlive.repositories;

import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.entities.AccommodationUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationUnitRepository extends JpaRepository<AccommodationUnit, Long> {
    
    @Query("SELECT DISTINCT au FROM AccommodationUnit au " +
            "LEFT JOIN FETCH au.accommodation " +
            "LEFT JOIN FETCH au.dictionaries d " +
            "LEFT JOIN FETCH d.dictionary " +
            "LEFT JOIN FETCH au.tariffs t " +
            "LEFT JOIN FETCH t.rangeType " +
            "LEFT JOIN FETCH au.images " +
            "WHERE au.id = :id AND au.isDeleted = false")
    Optional<AccommodationUnit> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT DISTINCT au FROM AccommodationUnit au " +
            "LEFT JOIN FETCH au.accommodation " +
            "LEFT JOIN FETCH au.dictionaries d " +
            "LEFT JOIN FETCH d.dictionary " +
            "LEFT JOIN FETCH au.tariffs t " +
            "LEFT JOIN FETCH t.rangeType " +
            "LEFT JOIN FETCH au.images " +
            "WHERE au.accommodation.id = :accommodationId AND au.isDeleted = false")
    List<AccommodationUnit> findByAccommodationIdAndIsDeletedFalse(Long accommodationId);

    @Query("SELECT DISTINCT au FROM AccommodationUnit au " +
            "LEFT JOIN FETCH au.accommodation " +
            "WHERE (:#{#params.accommodationId} IS NULL OR au.accommodation.id = :#{#params.accommodationId}) " +
            "AND (:#{#params.unitType} IS NULL OR :#{#params.unitType} = '' OR UPPER(au.unitType) = UPPER(:#{#params.unitType})) " +
            "AND (:#{#params.isAvailable} IS NULL OR au.isAvailable = :#{#params.isAvailable}) " +
            "AND (:#{#params.isDeleted} IS NULL OR au.isDeleted = :#{#params.isDeleted}) " +
            "AND (:#{#params.name} IS NULL OR :#{#params.name} = '' OR UPPER(au.name) LIKE UPPER(CONCAT('%', :#{#params.name}, '%'))) " +
            "AND (:#{#params.minCapacity} IS NULL OR au.capacity >= :#{#params.minCapacity}) " +
            "AND (:#{#params.maxCapacity} IS NULL OR au.capacity <= :#{#params.maxCapacity}) " +
            "AND (:#{#params.minArea} IS NULL OR au.area >= :#{#params.minArea}) " +
            "AND (:#{#params.maxArea} IS NULL OR au.area <= :#{#params.maxArea})")
    Page<AccommodationUnit> findWithFilters(@Param("params") AccommodationUnitSearchParams params, Pageable pageable);
}
