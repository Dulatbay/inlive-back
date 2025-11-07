package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.PriceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceRequestRepository extends JpaRepository<PriceRequest, Long> {

    Optional<PriceRequest> findByIdAndIsDeletedFalse(Long id);

    /**
     * Находит все активные (не скрытые) заявки на цену для указанного accommodation-unit
     */
    @Query("SELECT pr FROM PriceRequest pr " +
           "WHERE pr.unit.id = :unitId " +
           "AND pr.isDeleted = false " +
           "ORDER BY pr.createdAt DESC")
    Page<PriceRequest> findActiveByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    /**
     * Находит все заявки на цену для указанной заявки на поиск
     */
    @Query("SELECT pr FROM PriceRequest pr " +
           "WHERE pr.searchRequest.id = :searchRequestId " +
           "AND pr.isDeleted = false " +
           "ORDER BY pr.createdAt DESC")
    Page<PriceRequest> findActiveBySearchRequestId(@Param("searchRequestId") Long searchRequestId, Pageable pageable);

    /**
     * Проверяет существование активной заявки на цену для данной пары searchRequest + unit
     */
    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END FROM PriceRequest pr " +
           "WHERE pr.searchRequest.id = :searchRequestId " +
           "AND pr.unit.id = :unitId " +
           "AND pr.isDeleted = false")
    boolean existsBySearchRequestIdAndUnitId(@Param("searchRequestId") Long searchRequestId,
                                             @Param("unitId") Long unitId);
}

