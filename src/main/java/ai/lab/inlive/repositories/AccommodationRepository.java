package ai.lab.inlive.repositories;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.entities.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Optional<Accommodation> findByIdAndIsDeletedFalse(Long id);

    List<Accommodation> findByApprovedAndIsDeletedFalse(Boolean approved);

    List<Accommodation> findByApprovedIsNullAndIsDeletedFalse();

    List<Accommodation> findByOwnerIdIdAndIsDeletedFalse(Long ownerId);

    @Query(value = """
            SELECT a.*
            FROM accommodations a
            LEFT JOIN users u ON a.owner_id = u.id
            LEFT JOIN districts d ON a.district_id = d.id
            LEFT JOIN cities c ON a.city_id = c.id
            WHERE (CAST(:#{#params.cityId} AS BIGINT) IS NULL OR a.city_id = CAST(:#{#params.cityId} AS BIGINT))
              AND (CAST(:#{#params.districtId} AS BIGINT) IS NULL OR a.district_id = CAST(:#{#params.districtId} AS BIGINT))
              AND (CAST(:#{#params.approved} AS BOOLEAN) IS NULL OR a.is_approved = CAST(:#{#params.approved} AS BOOLEAN))
              AND (CAST(:#{#params.ownerId} AS BIGINT) IS NULL OR a.owner_id = CAST(:#{#params.ownerId} AS BIGINT))
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
            WHERE (CAST(:#{#params.cityId} AS BIGINT) IS NULL OR a.city_id = CAST(:#{#params.cityId} AS BIGINT))
              AND (CAST(:#{#params.districtId} AS BIGINT) IS NULL OR a.district_id = CAST(:#{#params.districtId} AS BIGINT))
              AND (CAST(:#{#params.approved} AS BOOLEAN) IS NULL OR a.is_approved = CAST(:#{#params.approved} AS BOOLEAN))
              AND (CAST(:#{#params.ownerId} AS BIGINT) IS NULL OR a.owner_id = CAST(:#{#params.ownerId} AS BIGINT))
              AND (CAST(:#{#params.minRating} AS DOUBLE PRECISION) IS NULL OR a.rating >= CAST(:#{#params.minRating} AS DOUBLE PRECISION))
              AND (CAST(:#{#params.isDeleted} AS BOOLEAN) IS NULL OR a.is_deleted = CAST(:#{#params.isDeleted} AS BOOLEAN))
              AND (CAST(:#{#params.name} AS VARCHAR) IS NULL OR UPPER(a.name) LIKE UPPER(CONCAT('%', CAST(:#{#params.name} AS VARCHAR), '%')))
            """,
            nativeQuery = true)
    Page<Accommodation> findWithFilters(AccommodationSearchParams params, Pageable pageable);
}
