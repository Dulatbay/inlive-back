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
import ai.lab.inlive.exceptions.ForbiddenException;
import ai.lab.inlive.mappers.ReservationMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static ai.lab.inlive.constants.ValueConstants.ZONE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final PriceRequestRepository priceRequestRepository;
    private final AccSearchRequestRepository accSearchRequestRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final AccommodationRepository accommodationRepository;
    private final ReservationMapper reservationMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void createReservation(ReservationCreateRequest request) {
        log.info("Creating reservation for price request: {}", request.getPriceRequestId());

        PriceRequest priceRequest = priceRequestRepository
                .findByIdAndIsDeletedFalse(request.getPriceRequestId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        messageSource.getMessage("services.reservation.priceRequestNotFound", 
                                new Object[]{request.getPriceRequestId()}, LocaleContextHolder.getLocale())));

        if (priceRequest.getClientResponseStatus() != ClientResponseStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.priceRequestNotAccepted", null, LocaleContextHolder.getLocale()));
        }

        if (reservationRepository.existsByPriceRequestId(request.getPriceRequestId())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.alreadyExists", null, LocaleContextHolder.getLocale()));
        }

        Reservation reservation = new Reservation();
        reservation.setPriceRequest(priceRequest);
        reservation.setUnit(priceRequest.getUnit());
        reservation.setSearchRequest(priceRequest.getSearchRequest());
        reservation.setApprovedBy(priceRequest.getSearchRequest().getAuthor());
        reservation.setStatus(ReservationStatus.WAITING_TO_APPROVE);
        reservation.setNeedToPay(false);

        AccSearchRequest searchRequest = priceRequest.getSearchRequest();
        searchRequest.setStatus(SearchRequestStatus.WAIT_TO_RESERVATION);
        accSearchRequestRepository.save(searchRequest);

        reservationRepository.save(reservation);
        log.info("Successfully created reservation with ID: {} and status: {}",
                reservation.getId(), reservation.getStatus());
    }

    @Override
    @Transactional
    public void updateReservationStatus(Long reservationId, ReservationUpdateRequest request) {
        log.info("Updating reservation: {} with status: {}", reservationId, request.getStatus());

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(reservationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        messageSource.getMessage("services.reservation.notFound", 
                                new Object[]{reservationId}, LocaleContextHolder.getLocale())));

        if (reservation.getStatus() != ReservationStatus.WAITING_TO_APPROVE) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.notWaiting", 
                            new Object[]{reservation.getStatus()}, LocaleContextHolder.getLocale()));
        }

        if (request.getStatus() != ReservationStatus.APPROVED &&
                request.getStatus() != ReservationStatus.REJECTED) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.invalidStatus", null, LocaleContextHolder.getLocale()));
        }

        reservation.setStatus(request.getStatus());

        if (request.getStatus() == ReservationStatus.APPROVED) {
            AccSearchRequest searchRequest = reservation.getSearchRequest();
            searchRequest.setStatus(SearchRequestStatus.FINISHED);
            accSearchRequestRepository.save(searchRequest);
            log.info("Reservation {} approved. Search request {} marked as FINISHED",
                    reservationId, searchRequest.getId());
        } else {
            log.info("Reservation {} rejected by SUPER_MANAGER", reservationId);
            AccSearchRequest searchRequest = reservation.getSearchRequest();
            searchRequest.setStatus(SearchRequestStatus.PRICE_REQUEST_PENDING);
            accSearchRequestRepository.save(searchRequest);
        }

        reservationRepository.save(reservation);
        log.info("Successfully updated reservation with ID: {} to status: {}",
                reservationId, request.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        log.info("Fetching reservation by ID: {}", id);

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        messageSource.getMessage("services.reservation.notFound", 
                                new Object[]{id}, LocaleContextHolder.getLocale())));

        return reservationMapper.toDto(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByUnitId(Long unitId, Pageable pageable) {
        log.info("Fetching reservations for unit: {}", unitId);

        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        messageSource.getMessage("services.reservation.accommodationUnitNotFound", 
                                new Object[]{unitId}, LocaleContextHolder.getLocale())));

        Page<Reservation> reservations = reservationRepository.findActiveByUnitId(unitId, pageable);

        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByAccommodationId(Long accommodationId, Pageable pageable) {
        log.info("Fetching reservations for accommodation: {}", accommodationId);

        accommodationRepository.findByIdAndIsDeletedFalse(accommodationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_NOT_FOUND",
                        messageSource.getMessage("services.reservation.accommodationNotFound", 
                                new Object[]{accommodationId}, LocaleContextHolder.getLocale())));

        Page<Reservation> reservations = reservationRepository.findByAccommodationId(accommodationId, pageable);

        log.info("Found {} reservations for accommodation {}", reservations.getTotalElements(), accommodationId);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getPendingReservationsByUnitId(Long unitId, Pageable pageable) {
        log.info("Fetching pending reservations for unit: {}", unitId);

        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        messageSource.getMessage("services.reservation.accommodationUnitNotFound", 
                                new Object[]{unitId}, LocaleContextHolder.getLocale())));

        Page<Reservation> reservations = reservationRepository.findPendingByUnitId(unitId, pageable);
        log.info("Found {} pending reservations for unit {}", reservations.getTotalElements(), unitId);

        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsBySearchRequestId(Long searchRequestId, Pageable pageable) {
        log.info("Fetching reservations for search request: {}", searchRequestId);

        accSearchRequestRepository.findByIdAndIsDeletedFalse(searchRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        messageSource.getMessage("services.reservation.searchRequestNotFound", 
                                new Object[]{searchRequestId}, LocaleContextHolder.getLocale())));

        Page<Reservation> reservations = reservationRepository.findBySearchRequestId(searchRequestId, pageable);

        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional
    public void updateFinalStatus(Long reservationId, ReservationFinalStatusUpdateRequest request) {
        log.info("Updating final status for reservation: {} to status: {}", reservationId, request.getStatus());

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(reservationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        messageSource.getMessage("services.reservation.notFound", 
                                new Object[]{reservationId}, LocaleContextHolder.getLocale())));

        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.notApproved", 
                            new Object[]{reservation.getStatus()}, LocaleContextHolder.getLocale()));
        }

        if (request.getStatus() != ReservationStatus.FINISHED_SUCCESSFUL &&
                request.getStatus() != ReservationStatus.CLIENT_DIDNT_CAME) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.invalidFinalStatus", null, LocaleContextHolder.getLocale()));
        }

        reservation.setStatus(request.getStatus());

        reservationRepository.save(reservation);

        if (request.getStatus() == ReservationStatus.FINISHED_SUCCESSFUL) {
            log.info("Reservation {} marked as FINISHED_SUCCESSFUL - client checked in successfully", reservationId);
        } else {
            log.info("Reservation {} marked as CLIENT_DIDNT_CAME - client did not show up", reservationId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyReservations(String clientId, Pageable pageable) {
        log.info("Fetching reservations for client: {}", clientId);
        Page<Reservation> reservations = reservationRepository.findByClientId(clientId, pageable);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, String clientId) {
        log.info("Client {} attempting to cancel reservation {}", clientId, reservationId);

        Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(reservationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND",
                        messageSource.getMessage("services.reservation.notFound", 
                                new Object[]{reservationId}, LocaleContextHolder.getLocale())));

        if (!reservation.getApprovedBy().getKeycloakId().equals(clientId)) {
            throw new ForbiddenException(
                    messageSource.getMessage("services.reservation.accessDenied", null, LocaleContextHolder.getLocale()));
        }

        if (reservation.getStatus() != ReservationStatus.WAITING_TO_APPROVE &&
                reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.invalidStatusForCancellation", 
                            new Object[]{reservation.getStatus()}, LocaleContextHolder.getLocale()));
        }

        AccSearchRequest searchRequest = reservation.getSearchRequest();
        LocalDateTime checkInDate = searchRequest.getFromDate();
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDateTime oneDayBeforeCheckIn = checkInDate.minusDays(1);

        if (now.isAfter(oneDayBeforeCheckIn)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("services.reservation.tooLateToCancel", 
                            new Object[]{checkInDate, now}, LocaleContextHolder.getLocale()));
        }

        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);

        log.info("Successfully cancelled reservation {} by client {}", reservationId, clientId);
    }
}
