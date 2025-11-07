package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.AccSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccSearchRequestRepository extends JpaRepository<AccSearchRequest, Long> {

    Optional<AccSearchRequest> findByIdAndIsDeletedFalse(Long id);

    /**
     * Находит релевантные заявки для указанного accommodation-unit
     * Критерии релевантности:
     * - Статус заявки должен быть один из: OPEN_TO_PRICE_REQUEST, PRICE_REQUEST_PENDING, WAIT_TO_RESERVATION
     * - Рейтинг accommodation должен быть в диапазоне fromRating - toRating заявки
     * - Район accommodation должен быть в списке районов заявки
     * - Тип unit должен быть в списке типов заявки
     * - Все услуги (SERVICES) заявки должны быть у unit
     * - Все условия (CONDITIONS) заявки должны быть у unit
     */
    @Query(value = """
            SELECT DISTINCT asr.*
            FROM acc_search_request asr
            INNER JOIN accommodation_units au ON au.id = :unitId AND au.is_deleted = FALSE
            INNER JOIN accommodations acc ON acc.id = au.acc_id AND acc.is_deleted = FALSE
            -- Проверка статуса
            WHERE asr.status IN ('OPEN_TO_PRICE_REQUEST', 'PRICE_REQUEST_PENDING', 'WAIT_TO_RESERVATION')
              AND asr.is_deleted = FALSE
            -- Проверка рейтинга
              AND (asr.from_rating IS NULL OR acc.rating >= asr.from_rating)
              AND (asr.to_rating IS NULL OR acc.rating <= asr.to_rating)
            -- Проверка района
              AND EXISTS (
                  SELECT 1 FROM acc_search_request_district asrd
                  WHERE asrd.search_request_id = asr.id
                    AND asrd.district_id = acc.district_id
                    AND asrd.is_deleted = FALSE
              )
            -- Проверка типа недвижимости
              AND EXISTS (
                  SELECT 1 FROM acc_search_request_unit_type asrut
                  WHERE asrut.search_request_id = asr.id
                    AND asrut.unit_type = au.unit_type
                    AND asrut.is_deleted = FALSE
              )
            -- Проверка что все услуги (SERVICES) из заявки есть у unit
              AND NOT EXISTS (
                  SELECT 1 FROM acc_search_request_dictionary asrd
                  INNER JOIN dictionaries d ON d.id = asrd.dictionary_id AND d."key" = 'ACC_SERVICE'
                  WHERE asrd.search_request_id = asr.id
                    AND asrd.is_deleted = FALSE
                    AND NOT EXISTS (
                        SELECT 1 FROM acc_unit_dictionary aud
                        WHERE aud.accommodation_unit_id = au.id
                          AND aud.dictionary_id = d.id
                          AND aud.is_deleted = FALSE
                    )
              )
            -- Проверка что все условия (CONDITIONS) из заявки есть у unit
              AND NOT EXISTS (
                  SELECT 1 FROM acc_search_request_dictionary asrd
                  INNER JOIN dictionaries d ON d.id = asrd.dictionary_id AND d."key" = 'ACC_CONDITION'
                  WHERE asrd.search_request_id = asr.id
                    AND asrd.is_deleted = FALSE
                    AND NOT EXISTS (
                        SELECT 1 FROM acc_unit_dictionary aud
                        WHERE aud.accommodation_unit_id = au.id
                          AND aud.dictionary_id = d.id
                          AND aud.is_deleted = FALSE
                    )
              )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT asr.id)
            FROM acc_search_request asr
            INNER JOIN accommodation_units au ON au.id = :unitId AND au.is_deleted = FALSE
            INNER JOIN accommodations acc ON acc.id = au.acc_id AND acc.is_deleted = FALSE
            WHERE asr.status IN ('OPEN_TO_PRICE_REQUEST', 'PRICE_REQUEST_PENDING', 'WAIT_TO_RESERVATION')
              AND asr.is_deleted = FALSE
              AND (asr.from_rating IS NULL OR acc.rating >= asr.from_rating)
              AND (asr.to_rating IS NULL OR acc.rating <= asr.to_rating)
              AND EXISTS (
                  SELECT 1 FROM acc_search_request_district asrd
                  WHERE asrd.search_request_id = asr.id
                    AND asrd.district_id = acc.district_id
                    AND asrd.is_deleted = FALSE
              )
              AND EXISTS (
                  SELECT 1 FROM acc_search_request_unit_type asrut
                  WHERE asrut.search_request_id = asr.id
                    AND asrut.unit_type = au.unit_type
                    AND asrut.is_deleted = FALSE
              )
              AND NOT EXISTS (
                  SELECT 1 FROM acc_search_request_dictionary asrd
                  INNER JOIN dictionaries d ON d.id = asrd.dictionary_id AND d."key" = 'ACC_SERVICE'
                  WHERE asrd.search_request_id = asr.id
                    AND asrd.is_deleted = FALSE
                    AND NOT EXISTS (
                        SELECT 1 FROM acc_unit_dictionary aud
                        WHERE aud.accommodation_unit_id = au.id
                          AND aud.dictionary_id = d.id
                          AND aud.is_deleted = FALSE
                    )
              )
              AND NOT EXISTS (
                  SELECT 1 FROM acc_search_request_dictionary asrd
                  INNER JOIN dictionaries d ON d.id = asrd.dictionary_id AND d."key" = 'ACC_CONDITION'
                  WHERE asrd.search_request_id = asr.id
                    AND asrd.is_deleted = FALSE
                    AND NOT EXISTS (
                        SELECT 1 FROM acc_unit_dictionary aud
                        WHERE aud.accommodation_unit_id = au.id
                          AND aud.dictionary_id = d.id
                          AND aud.is_deleted = FALSE
                    )
              )
            """,
            nativeQuery = true)
    Page<AccSearchRequest> findRelevantRequestsForUnit(@Param("unitId") Long unitId, Pageable pageable);
}
