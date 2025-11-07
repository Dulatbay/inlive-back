package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findAllByIsDeletedFalse();

    @Query(value = """
                SELECT d.*
                FROM districts d
                LEFT JOIN cities c ON d.city_id = c.id
                WHERE c.id = :cityId
                AND d.is_deleted = false
            """, nativeQuery = true)
    List<District> findByCityIdAndIsDeletedFalse(@Param(value = "cityId") Long cityId);
}
