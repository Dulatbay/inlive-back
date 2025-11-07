package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.ReservationCreateRequest;
import ai.lab.inlive.dto.request.ReservationUpdateRequest;
import ai.lab.inlive.dto.request.ReservationFinalStatusUpdateRequest;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.ClientResponseStatus;
import ai.lab.inlive.entities.enums.ReservationStatus;
import ai.lab.inlive.entities.enums.SearchRequestStatus;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.ReservationMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final PriceRequestRepository priceRequestRepository;
    private final AccSearchRequestRepository accSearchRequestRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        log.info("Creating reservation for price request: {}", request.getPriceRequestId());

        // Проверяем что price request существует
        PriceRequest priceRequest = priceRequestRepository
                .findByIdAndIsDeletedFalse(request.getPriceRequestId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + request.getPriceRequestId()));

        // Проверяем что клиент подтвердил заявку на цену
        if (priceRequest.getClientResponseStatus() != ClientResponseStatus.ACCEPTED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "PRICE_REQUEST_NOT_ACCEPTED",
                    "Price request must be accepted by client before creating reservation");
        }

        // Проверяем что для этой price request еще нет бронирования
        if (reservationRepository.existsByPriceRequestId(request.getPriceRequestId())) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "RESERVATION_ALREADY_EXISTS",
                    "Reservation already exists for this price request");
        }

        // Создаем бронирование
        Reservation reservation = new Reservation();
        reservation.setPriceRequest(priceRequest);
        reservation.setUnit(priceRequest.getUnit());
        reservation.setSearchRequest(priceRequest.getSearchRequest());
        reservation.setApprovedBy(priceRequest.getSearchRequest().getAuthor()); // Клиент, создавший заявку
        reservation.setStatus(ReservationStatus.WAITING_TO_APPROVE); // Ожидает подтверждения SUPER_MANAGER
        reservation.setNeedToPay(false); // На стадии MVP предоплата не требуется

        // Обновляем статус search request на WAIT_TO_RESERVATION
        AccSearchRequest searchRequest = priceRequest.getSearchRequest();
        searchRequest.setStatus(SearchRequestStatus.WAIT_TO_RESERVATION);
        accSearchRequestRepository.save(searchRequest);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Successfully created reservation with ID: {} and status: {}",
                saved.getId(), saved.getStatus());

        return reservationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservationStatus(Long reservationId, ReservationUpdateRequest request) {
        log.info("Updating reservation: {} with status: {}", reservationId, request.getStatus());

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(reservationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        "Reservation not found with ID: " + reservationId));

        // Проверяем что бронирование в статусе WAITING_TO_APPROVE
        if (reservation.getStatus() != ReservationStatus.WAITING_TO_APPROVE) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "RESERVATION_NOT_WAITING",
                    "Reservation is not waiting for approval. Current status: " + reservation.getStatus());
        }

        // Проверяем что новый статус - APPROVED или REJECTED
        if (request.getStatus() != ReservationStatus.APPROVED &&
            request.getStatus() != ReservationStatus.REJECTED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_STATUS",
                    "Status must be APPROVED or REJECTED");
        }

        // Обновляем статус бронирования
        reservation.setStatus(request.getStatus());

        // Если SUPER_MANAGER одобрил бронь, обновляем статус search request
        if (request.getStatus() == ReservationStatus.APPROVED) {
            AccSearchRequest searchRequest = reservation.getSearchRequest();
            searchRequest.setStatus(SearchRequestStatus.FINISHED);
            accSearchRequestRepository.save(searchRequest);
            log.info("Reservation {} approved. Search request {} marked as FINISHED",
                    reservationId, searchRequest.getId());
        } else {
            log.info("Reservation {} rejected by SUPER_MANAGER", reservationId);
            // При отказе возвращаем search request в статус PRICE_REQUEST_PENDING
            AccSearchRequest searchRequest = reservation.getSearchRequest();
            searchRequest.setStatus(SearchRequestStatus.PRICE_REQUEST_PENDING);
            accSearchRequestRepository.save(searchRequest);
        }

        Reservation updated = reservationRepository.save(reservation);
        log.info("Successfully updated reservation with ID: {} to status: {}",
                reservationId, request.getStatus());

        return reservationMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        log.info("Fetching reservation by ID: {}", id);

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        "Reservation not found with ID: " + id));

        return reservationMapper.toDto(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByUnitId(Long unitId, Pageable pageable) {
        log.info("Fetching reservations for unit: {}", unitId);

        // Проверяем что unit существует
        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        Page<Reservation> reservations = reservationRepository.findActiveByUnitId(unitId, pageable);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getPendingReservationsByUnitId(Long unitId, Pageable pageable) {
        log.info("Fetching pending reservations for unit: {}", unitId);

        // Проверяем что unit существует
        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        Page<Reservation> reservations = reservationRepository.findPendingByUnitId(unitId, pageable);
        log.info("Found {} pending reservations for unit {}", reservations.getTotalElements(), unitId);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsBySearchRequestId(Long searchRequestId, Pageable pageable) {
        log.info("Fetching reservations for search request: {}", searchRequestId);

        // Проверяем что search request существует
        accSearchRequestRepository.findByIdAndIsDeletedFalse(searchRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        "Search request not found with ID: " + searchRequestId));

        Page<Reservation> reservations = reservationRepository
                .findBySearchRequestId(searchRequestId, pageable);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional
    public ReservationResponse updateFinalStatus(Long reservationId, ReservationFinalStatusUpdateRequest request) {
        log.info("Updating final status for reservation: {} to status: {}", reservationId, request.getStatus());

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(reservationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        "Reservation not found with ID: " + reservationId));

        // Проверяем что бронирование в статусе APPROVED
        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "RESERVATION_NOT_APPROVED",
                    "Reservation must be in APPROVED status. Current status: " + reservation.getStatus());
        }

        // Проверяем что новый статус - FINISHED_SUCCESSFUL или CLIENT_DIDNT_CAME
        if (request.getStatus() != ReservationStatus.FINISHED_SUCCESSFUL &&
            request.getStatus() != ReservationStatus.CLIENT_DIDNT_CAME) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_FINAL_STATUS",
                    "Final status must be FINISHED_SUCCESSFUL or CLIENT_DIDNT_CAME");
        }

        // Обновляем статус бронирования
        reservation.setStatus(request.getStatus());

        Reservation updated = reservationRepository.save(reservation);

        if (request.getStatus() == ReservationStatus.FINISHED_SUCCESSFUL) {
            log.info("Reservation {} marked as FINISHED_SUCCESSFUL - client checked in successfully", reservationId);
        } else {
            log.info("Reservation {} marked as CLIENT_DIDNT_CAME - client did not show up", reservationId);
        }

        return reservationMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyReservations(Long clientId, Pageable pageable) {
        log.info("Fetching reservations for client: {}", clientId);
        Page<Reservation> reservations = reservationRepository.findByClientId(clientId, pageable);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, Long clientId) {
        log.info("Client {} attempting to cancel reservation {}", clientId, reservationId);

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(reservationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        "Reservation not found with ID: " + reservationId));

        // Проверяем что клиент является владельцем брони
        if (!reservation.getApprovedBy().getId().equals(clientId)) {
            throw new DbObjectNotFoundException(HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    "You can only cancel your own reservations");
        }

        // Проверяем что бронь в статусе WAITING_TO_APPROVE или APPROVED
        if (reservation.getStatus() != ReservationStatus.WAITING_TO_APPROVE &&
            reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_STATUS_FOR_CANCELLATION",
                    "Can only cancel reservations with status WAITING_TO_APPROVE or APPROVED. Current status: " +
                    reservation.getStatus());
        }

        // Проверяем что отмена происходит минимум за 1 день до заезда
        AccSearchRequest searchRequest = reservation.getSearchRequest();
        java.time.LocalDateTime checkInDate = searchRequest.getFromDate();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime oneDayBeforeCheckIn = checkInDate.minusDays(1);

        if (now.isAfter(oneDayBeforeCheckIn)) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "TOO_LATE_TO_CANCEL",
                    "Reservations can only be cancelled at least 1 day before check-in date. Check-in: " +
                    checkInDate + ", Current time: " + now);
        }

        // Отменяем бронирование
        reservation.setStatus(ReservationStatus.CANCELED);
        Reservation canceled = reservationRepository.save(reservation);

        log.info("Successfully cancelled reservation {} by client {}", reservationId, clientId);

        return reservationMapper.toDto(canceled);
    }
}
