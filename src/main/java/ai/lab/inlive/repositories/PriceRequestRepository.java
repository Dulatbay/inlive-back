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
    
    @Query("SELECT DISTINCT pr FROM PriceRequest pr " +
           "LEFT JOIN FETCH pr.unit u " +
           "LEFT JOIN FETCH u.accommodation " +
           "LEFT JOIN FETCH pr.searchRequest " +
           "WHERE pr.id = :id AND pr.isDeleted = false")
    Optional<PriceRequest> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT DISTINCT pr FROM PriceRequest pr " +
           "LEFT JOIN FETCH pr.unit u " +
           "LEFT JOIN FETCH u.accommodation " +
           "LEFT JOIN FETCH pr.searchRequest " +
           "WHERE pr.unit.id = :unitId " +
           "AND pr.isDeleted = false " +
           "ORDER BY pr.createdAt DESC")
    Page<PriceRequest> findActiveByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    @Query("SELECT DISTINCT pr FROM PriceRequest pr " +
           "LEFT JOIN FETCH pr.unit u " +
           "LEFT JOIN FETCH u.accommodation " +
           "LEFT JOIN FETCH pr.searchRequest " +
           "WHERE pr.searchRequest.id = :searchRequestId " +
           "AND pr.isDeleted = false " +
           "ORDER BY pr.createdAt DESC")
    Page<PriceRequest> findActiveBySearchRequestId(@Param("searchRequestId") Long searchRequestId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END FROM PriceRequest pr " +
           "WHERE pr.searchRequest.id = :searchRequestId " +
           "AND pr.unit.id = :unitId " +
           "AND pr.isDeleted = false")
    boolean existsBySearchRequestIdAndUnitId(@Param("searchRequestId") Long searchRequestId,
                                             @Param("unitId") Long unitId);
}

