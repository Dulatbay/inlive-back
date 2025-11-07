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

    /**
     * Находит все активные бронирования для указанного accommodation-unit
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.unit.id = :unitId " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createdAt DESC")
    Page<Reservation> findActiveByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    /**
     * Находит все бронирования со статусом WAITING_TO_APPROVE для указанного unit
     * (бронирования, ожидающие подтверждения SUPER_MANAGER)
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.unit.id = :unitId " +
           "AND r.status = 'WAITING_TO_APPROVE' " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createdAt DESC")
    Page<Reservation> findPendingByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    /**
     * Находит все бронирования для указанной заявки на поиск
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.searchRequest.id = :searchRequestId " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createdAt DESC")
    Page<Reservation> findBySearchRequestId(@Param("searchRequestId") Long searchRequestId, Pageable pageable);

    /**
     * Находит бронирование по price request
     */
    Optional<Reservation> findByPriceRequestIdAndIsDeletedFalse(Long priceRequestId);

    /**
     * Проверяет существование бронирования для price request
     */
    boolean existsByPriceRequestId(Long priceRequestId);

    /**
     * Находит все бронирования клиента
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.approvedBy.id = :clientId " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createdAt DESC")
    Page<Reservation> findByClientId(@Param("clientId") Long clientId, Pageable pageable);
}
