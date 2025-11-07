package ai.lab.inlive.repositories;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.entities.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Optional<Accommodation> findByIdAndIsDeletedFalse(Long id);

    @Query(value = """
            SELECT a.*
            FROM accommodations a
            LEFT JOIN users u ON a.owner_id = u.id
            LEFT JOIN districts d ON a.district_id = d.id
            LEFT JOIN cities c ON a.city_id = c.id
            WHERE (CAST(:#{#params.cityId} AS BIGINT) IS NULL OR c.id = CAST(:#{#params.cityId} AS BIGINT))
              AND (CAST(:#{#params.districtId} AS BIGINT) IS NULL OR d.id = CAST(:#{#params.districtId} AS BIGINT))
              AND (CAST(:#{#params.approved} AS BOOLEAN) IS NULL OR a.is_approved = CAST(:#{#params.approved} AS BOOLEAN))
              AND (CAST(:#{#params.ownerId} AS BIGINT) IS NULL OR u.id = CAST(:#{#params.ownerId} AS BIGINT))
              AND (CAST(:#{#params.minRating} AS DOUBLE PRECISION) IS NULL OR a.rating >= CAST(:#{#params.minRating} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR a.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(a.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM accommodations a
            LEFT JOIN users u ON a.owner_id = u.id
            LEFT JOIN districts d ON a.district_id = d.id
            LEFT JOIN cities c ON a.city_id = c.id
            WHERE (CAST(:#{#params.cityId} AS BIGINT) IS NULL OR c.id = CAST(:#{#params.cityId} AS BIGINT))
              AND (CAST(:#{#params.districtId} AS BIGINT) IS NULL OR d.id = CAST(:#{#params.districtId} AS BIGINT))
              AND (CAST(:#{#params.approved} AS BOOLEAN) IS NULL OR a.is_approved = CAST(:#{#params.approved} AS BOOLEAN))
              AND (CAST(:#{#params.ownerId} AS BIGINT) IS NULL OR u.id = CAST(:#{#params.ownerId} AS BIGINT))
              AND (CAST(:#{#params.minRating} AS DOUBLE PRECISION) IS NULL OR a.rating >= CAST(:#{#params.minRating} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR a.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(a.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
            """,
            nativeQuery = true)
    Page<Accommodation> findWithFilters(@Param("params") AccommodationSearchParams params, Pageable pageable);

    @Query(value = """
            SELECT a.*
            FROM accommodations a
            LEFT JOIN users u ON a.owner_id = u.id
            LEFT JOIN districts d ON a.district_id = d.id
            LEFT JOIN cities c ON a.city_id = c.id
            WHERE (CAST(:#{#params.cityId} AS BIGINT) IS NULL OR c.id = CAST(:#{#params.cityId} AS BIGINT))
              AND (CAST(:#{#params.districtId} AS BIGINT) IS NULL OR d.id = CAST(:#{#params.districtId} AS BIGINT))
              AND (CAST(:#{#params.approved} AS BOOLEAN) IS NULL OR a.is_approved = CAST(:#{#params.approved} AS BOOLEAN))
              AND (CAST(:#{#params.ownerId} AS BIGINT) IS NULL OR u.id = CAST(:#{#params.ownerId} AS BIGINT))
              AND (CAST(:#{#params.minRating} AS DOUBLE PRECISION) IS NULL OR a.rating >= CAST(:#{#params.minRating} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR a.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(a.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
            AND a.owner_id = CAST(:ownerId AS BIGINT)
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM accommodations a
            LEFT JOIN users u ON a.owner_id = u.id
            LEFT JOIN districts d ON a.district_id = d.id
            LEFT JOIN cities c ON a.city_id = c.id
            WHERE (CAST(:#{#params.cityId} AS BIGINT) IS NULL OR c.id = CAST(:#{#params.cityId} AS BIGINT))
              AND (CAST(:#{#params.districtId} AS BIGINT) IS NULL OR d.id = CAST(:#{#params.districtId} AS BIGINT))
              AND (CAST(:#{#params.approved} AS BOOLEAN) IS NULL OR a.is_approved = CAST(:#{#params.approved} AS BOOLEAN))
              AND (CAST(:#{#params.ownerId} AS BIGINT) IS NULL OR u.id = CAST(:#{#params.ownerId} AS BIGINT))
              AND (CAST(:#{#params.minRating} AS DOUBLE PRECISION) IS NULL OR a.rating >= CAST(:#{#params.minRating} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR a.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(a.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
            AND a.owner_id = CAST(:ownerId AS BIGINT)
            """,
            nativeQuery = true)
    Page<Accommodation> findByOwnerIdWithFilters(@Param("ownerId") Long ownerId, @Param("params") AccommodationSearchParams params, Pageable pageable);
}
