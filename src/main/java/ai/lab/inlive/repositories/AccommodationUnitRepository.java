package ai.lab.inlive.repositories;

import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.entities.AccommodationUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccommodationUnitRepository extends JpaRepository<AccommodationUnit, Long> {
    Optional<AccommodationUnit> findByIdAndIsDeletedFalse(Long id);

    @Query(value = """
            SELECT au.*
            FROM accommodation_units au
            LEFT JOIN accommodations a ON au.acc_id = a.id
            WHERE (CAST(:#{#params.accommodationId} AS BIGINT) IS NULL OR a.id = CAST(:#{#params.accommodationId} AS BIGINT))
              AND (CAST(:#{#params.unitType} AS VARCHAR) IS NULL OR UPPER(au.unit_type) = UPPER(CAST(:#{#params.unitType} AS VARCHAR)))
              AND (CAST(:#{#params.isAvailable} AS BOOLEAN) IS NULL OR au.is_available = CAST(:#{#params.isAvailable} AS BOOLEAN))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR au.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(au.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
              AND (CAST(:#{#params.minCapacity} AS INTEGER) IS NULL OR au.capacity >= CAST(:#{#params.minCapacity} AS INTEGER))
              AND (CAST(:#{#params.maxCapacity} AS INTEGER) IS NULL OR au.capacity <= CAST(:#{#params.maxCapacity} AS INTEGER))
              AND (CAST(:#{#params.minArea} AS DOUBLE PRECISION) IS NULL OR au.area >= CAST(:#{#params.minArea} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.maxArea} AS DOUBLE PRECISION) IS NULL OR au.area <= CAST(:#{#params.maxArea} AS DOUBLE PRECISION))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM accommodation_units au
            LEFT JOIN accommodations a ON au.acc_id = a.id
            WHERE (CAST(:#{#params.accommodationId} AS BIGINT) IS NULL OR a.id = CAST(:#{#params.accommodationId} AS BIGINT))
              AND (CAST(:#{#params.unitType} AS VARCHAR) IS NULL OR UPPER(au.unit_type) = UPPER(CAST(:#{#params.unitType} AS VARCHAR)))
              AND (CAST(:#{#params.isAvailable} AS BOOLEAN) IS NULL OR au.is_available = CAST(:#{#params.isAvailable} AS BOOLEAN))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR au.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(au.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
              AND (CAST(:#{#params.minCapacity} AS INTEGER) IS NULL OR au.capacity >= CAST(:#{#params.minCapacity} AS INTEGER))
              AND (CAST(:#{#params.maxCapacity} AS INTEGER) IS NULL OR au.capacity <= CAST(:#{#params.maxCapacity} AS INTEGER))
              AND (CAST(:#{#params.minArea} AS DOUBLE PRECISION) IS NULL OR au.area >= CAST(:#{#params.minArea} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.maxArea} AS DOUBLE PRECISION) IS NULL OR au.area <= CAST(:#{#params.maxArea} AS DOUBLE PRECISION))
            """,
            nativeQuery = true)
    Page<AccommodationUnit> findWithFilters(@Param("params") AccommodationUnitSearchParams params, Pageable pageable);
}
