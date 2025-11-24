package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.unit.id = :unitId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findActiveByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.unit.accommodation.id = :accommodationId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findByAccommodationId(@Param("accommodationId") Long accommodationId, Pageable pageable);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.unit.id = :unitId " +
            "AND r.status = 'WAITING_TO_APPROVE' " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findPendingByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.searchRequest.id = :searchRequestId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findBySearchRequestId(@Param("searchRequestId") Long searchRequestId, Pageable pageable);

//    Optional<Reservation> findByPriceRequestIdAndIsDeletedFalse(Long priceRequestId);

    boolean existsByPriceRequestId(Long priceRequestId);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.approvedBy.keycloakId = :clientId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findByClientId(@Param("clientId") String clientId, Pageable pageable);
}
