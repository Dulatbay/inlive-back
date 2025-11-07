package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.ReservationCreateRequest;
import ai.lab.inlive.dto.request.ReservationFinalStatusUpdateRequest;
import ai.lab.inlive.dto.request.ReservationUpdateRequest;
import ai.lab.inlive.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    /**
     * Создать бронирование (автоматически после подтверждения клиентом price request)
     */
    ReservationResponse createReservation(ReservationCreateRequest request);

    /**
     * Обновить статус бронирования (для SUPER_MANAGER)
     * SUPER_MANAGER может принять (APPROVED) или отказать (REJECTED)
     */
    ReservationResponse updateReservationStatus(Long reservationId, ReservationUpdateRequest request);

    /**
     * Обновить финальный статус бронирования после прихода/неприхода клиента (для SUPER_MANAGER)
     * SUPER_MANAGER может установить FINISHED_SUCCESSFUL (клиент пришел) или CLIENT_DIDNT_CAME (не пришел)
     */
    ReservationResponse updateFinalStatus(Long reservationId, ReservationFinalStatusUpdateRequest request);

    /**
     * Получить бронирование по ID
     */
    ReservationResponse getReservationById(Long id);

    /**
     * Получить все бронирования для accommodation-unit
     */
    Page<ReservationResponse> getReservationsByUnitId(Long unitId, Pageable pageable);

    /**
     * Получить ожидающие подтверждения бронирования для accommodation-unit
     * (статус WAITING_TO_APPROVE)
     */
    Page<ReservationResponse> getPendingReservationsByUnitId(Long unitId, Pageable pageable);

    /**
     * Получить бронирования для search request
     */
    Page<ReservationResponse> getReservationsBySearchRequestId(Long searchRequestId, Pageable pageable);

    /**
     * Получить недавние бронирования клиента (для CLIENT)
     */
    Page<ReservationResponse> getMyReservations(Long clientId, Pageable pageable);

    /**
     * Отменить бронирование (только для CLIENT, за день до заезда)
     * Клиент может преждевременно отменить свою бронь (минимум за 1 день до заезда)
     */
    ReservationResponse cancelReservation(Long reservationId, Long clientId);
}
