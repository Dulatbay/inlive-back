package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.ReservationCreateRequest;
import ai.lab.inlive.dto.request.ReservationFinalStatusUpdateRequest;
import ai.lab.inlive.dto.request.ReservationUpdateRequest;
import ai.lab.inlive.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    void createReservation(ReservationCreateRequest request);

    void updateReservationStatus(Long reservationId, ReservationUpdateRequest request);

    void updateFinalStatus(Long reservationId, ReservationFinalStatusUpdateRequest request);

    ReservationResponse getReservationById(Long id);

    Page<ReservationResponse> getReservationsByUnitId(Long unitId, Pageable pageable);

    Page<ReservationResponse> getPendingReservationsByUnitId(Long unitId, Pageable pageable);

    Page<ReservationResponse> getReservationsBySearchRequestId(Long searchRequestId, Pageable pageable);

    Page<ReservationResponse> getMyReservations(String clientId, Pageable pageable);

    void cancelReservation(Long reservationId, String clientId);
}
