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

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "LEFT JOIN FETCH a.city " +
            "LEFT JOIN FETCH a.district " +
            "LEFT JOIN FETCH a.ownerId " +
            "LEFT JOIN FETCH a.approvedBy " +
            "LEFT JOIN FETCH a.dictionaries d " +
            "LEFT JOIN FETCH d.dictionary " +
            "LEFT JOIN FETCH a.images " +
            "WHERE a.id = :id AND a.isDeleted = false")
    Optional<Accommodation> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "LEFT JOIN FETCH a.city " +
            "LEFT JOIN FETCH a.district " +
            "LEFT JOIN FETCH a.ownerId " +
            "LEFT JOIN FETCH a.approvedBy " +
            "WHERE (:#{#params.cityId} IS NULL OR a.city.id = :#{#params.cityId}) " +
            "AND (:#{#params.districtId} IS NULL OR a.district.id = :#{#params.districtId}) " +
            "AND (:#{#params.approved} IS NULL OR a.approved = :#{#params.approved}) " +
            "AND (:#{#params.minRating} IS NULL OR a.rating >= :#{#params.minRating}) " +
            "AND (:#{#params.isDeleted} IS NULL OR a.isDeleted = :#{#params.isDeleted}) " +
            "AND (:#{#params.name} IS NULL OR :#{#params.name} = '' OR UPPER(a.name) LIKE UPPER(CONCAT('%', :#{#params.name}, '%')))")
    Page<Accommodation> findWithFilters(@Param("params") AccommodationSearchParams params, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "LEFT JOIN FETCH a.city " +
            "LEFT JOIN FETCH a.district " +
            "LEFT JOIN FETCH a.ownerId " +
            "LEFT JOIN FETCH a.approvedBy " +
            "WHERE a.ownerId.id = :ownerId " +
            "AND (:#{#params.cityId} IS NULL OR a.city.id = :#{#params.cityId}) " +
            "AND (:#{#params.districtId} IS NULL OR a.district.id = :#{#params.districtId}) " +
            "AND (:#{#params.approved} IS NULL OR a.approved = :#{#params.approved}) " +
            "AND (:#{#params.minRating} IS NULL OR a.rating >= :#{#params.minRating}) " +
            "AND (:#{#params.isDeleted} IS NULL OR a.isDeleted = :#{#params.isDeleted}) " +
            "AND (:#{#params.name} IS NULL OR :#{#params.name} = '' OR UPPER(a.name) LIKE UPPER(CONCAT('%', :#{#params.name}, '%')))")
    Page<Accommodation> findByOwnerIdWithFilters(@Param("ownerId") Long ownerId, @Param("params") AccommodationSearchParams params, Pageable pageable);
}
