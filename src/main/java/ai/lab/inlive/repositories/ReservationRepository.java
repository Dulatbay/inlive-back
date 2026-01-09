package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT DISTINCT r FROM Reservation r " +
            "LEFT JOIN FETCH r.unit u " +
            "LEFT JOIN FETCH u.accommodation acc " +
            "LEFT JOIN FETCH acc.city " +
            "LEFT JOIN FETCH acc.district " +
            "LEFT JOIN FETCH acc.ownerId " +
            "LEFT JOIN FETCH r.approvedBy " +
            "LEFT JOIN FETCH r.priceRequest " +
            "LEFT JOIN FETCH r.searchRequest " +
            "WHERE r.id = :id AND r.isDeleted = false")
    Optional<Reservation> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT DISTINCT r FROM Reservation r " +
            "LEFT JOIN FETCH r.unit u " +
            "LEFT JOIN FETCH u.accommodation acc " +
            "LEFT JOIN FETCH acc.city " +
            "LEFT JOIN FETCH acc.district " +
            "LEFT JOIN FETCH acc.ownerId " +
            "LEFT JOIN FETCH r.approvedBy " +
            "LEFT JOIN FETCH r.priceRequest " +
            "LEFT JOIN FETCH r.searchRequest " +
            "WHERE r.unit.id = :unitId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findActiveByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Reservation r " +
            "LEFT JOIN FETCH r.unit u " +
            "LEFT JOIN FETCH u.accommodation acc " +
            "LEFT JOIN FETCH acc.city " +
            "LEFT JOIN FETCH acc.district " +
            "LEFT JOIN FETCH acc.ownerId " +
            "LEFT JOIN FETCH r.approvedBy " +
            "LEFT JOIN FETCH r.priceRequest " +
            "LEFT JOIN FETCH r.searchRequest " +
            "WHERE r.unit.accommodation.id = :accommodationId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findByAccommodationId(@Param("accommodationId") Long accommodationId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Reservation r " +
            "LEFT JOIN FETCH r.unit u " +
            "LEFT JOIN FETCH u.accommodation acc " +
            "LEFT JOIN FETCH acc.city " +
            "LEFT JOIN FETCH acc.district " +
            "LEFT JOIN FETCH acc.ownerId " +
            "LEFT JOIN FETCH r.approvedBy " +
            "LEFT JOIN FETCH r.priceRequest " +
            "LEFT JOIN FETCH r.searchRequest " +
            "WHERE r.unit.id = :unitId " +
            "AND r.status = 'WAITING_TO_APPROVE' " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findPendingByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Reservation r " +
            "LEFT JOIN FETCH r.unit u " +
            "LEFT JOIN FETCH u.accommodation acc " +
            "LEFT JOIN FETCH acc.city " +
            "LEFT JOIN FETCH acc.district " +
            "LEFT JOIN FETCH acc.ownerId " +
            "LEFT JOIN FETCH r.approvedBy " +
            "LEFT JOIN FETCH r.priceRequest " +
            "LEFT JOIN FETCH r.searchRequest " +
            "WHERE r.searchRequest.id = :searchRequestId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findBySearchRequestId(@Param("searchRequestId") Long searchRequestId, Pageable pageable);

//    Optional<Reservation> findByPriceRequestIdAndIsDeletedFalse(Long priceRequestId);

    boolean existsByPriceRequestId(Long priceRequestId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.unit.id = :unitId " +
            "AND r.isDeleted = false " +
            "AND r.status IN ('WAITING_TO_APPROVE', 'ACCEPTED') " +
            "AND r.searchRequest.fromDate < :checkOutDate " +
            "AND r.searchRequest.toDate > :checkInDate")
    boolean isUnitReservedForPeriod(@Param("unitId") Long unitId,
                                   @Param("checkInDate") LocalDateTime checkInDate,
                                   @Param("checkOutDate") LocalDateTime checkOutDate);

    @Query("SELECT DISTINCT r FROM Reservation r " +
            "LEFT JOIN FETCH r.unit u " +
            "LEFT JOIN FETCH u.accommodation acc " +
            "LEFT JOIN FETCH acc.city " +
            "LEFT JOIN FETCH acc.district " +
            "LEFT JOIN FETCH acc.ownerId " +
            "LEFT JOIN FETCH r.approvedBy " +
            "LEFT JOIN FETCH r.priceRequest " +
            "LEFT JOIN FETCH r.searchRequest " +
            "WHERE r.approvedBy.keycloakId = :clientId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findByClientId(@Param("clientId") String clientId, Pageable pageable);
}
