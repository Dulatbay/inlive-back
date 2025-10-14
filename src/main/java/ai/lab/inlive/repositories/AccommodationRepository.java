package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Optional<Accommodation> findByIdAndIsDeletedFalse(Long id);

    Page<Accommodation> findAllByIsDeletedFalse(Pageable pageable);

    List<Accommodation> findByApprovedAndIsDeletedFalse(Boolean approved);

    List<Accommodation> findByOwnerIdIdAndIsDeletedFalse(Long ownerId);

    @Query(value = "SELECT * FROM accommodations a WHERE 1=1 " +
           "AND (:cityId IS NULL OR a.city_id = :cityId) " +
           "AND (:districtId IS NULL OR a.district_id = :districtId) " +
           "AND (:approved IS NULL OR a.approved = :approved) " +
           "AND (:ownerId IS NULL OR a.owner_id = :ownerId) " +
           "AND (:minRating IS NULL OR a.rating >= :minRating) " +
           "AND (:isDeleted IS NULL OR a.is_deleted = :isDeleted) " +
           "AND (:name IS NULL OR UPPER(a.name::text) LIKE UPPER('%' || :name || '%'))",
           countQuery = "SELECT COUNT(*) FROM accommodations a WHERE 1=1 " +
           "AND (:cityId IS NULL OR a.city_id = :cityId) " +
           "AND (:districtId IS NULL OR a.district_id = :districtId) " +
           "AND (:approved IS NULL OR a.approved = :approved) " +
           "AND (:ownerId IS NULL OR a.owner_id = :ownerId) " +
           "AND (:minRating IS NULL OR a.rating >= :minRating) " +
           "AND (:isDeleted IS NULL OR a.is_deleted = :isDeleted) " +
           "AND (:name IS NULL OR UPPER(a.name::text) LIKE UPPER('%' || :name || '%'))",
           nativeQuery = true)
    Page<Accommodation> findWithFilters(
            @Param("cityId") Long cityId,
            @Param("districtId") Long districtId,
            @Param("approved") Boolean approved,
            @Param("ownerId") Long ownerId,
            @Param("minRating") Double minRating,
            @Param("isDeleted") Boolean isDeleted,
            @Param("name") String name,
            Pageable pageable
    );
}
