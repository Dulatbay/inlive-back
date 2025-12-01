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

    @Query(value = """
                SELECT AVG(t.price)
                FROM acc_unit_tariffs t
                JOIN accommodation_units u ON t.accommodation_unit_id = u.id
                JOIN accommodations a ON u.acc_id = a.id
                WHERE a.district_id = :districtId
                AND a.is_deleted = false
                AND u.is_deleted = false
                AND t.is_deleted = false
            """, nativeQuery = true)
    Double calculateAveragePriceByDistrictId(@Param("districtId") Long districtId);
}
