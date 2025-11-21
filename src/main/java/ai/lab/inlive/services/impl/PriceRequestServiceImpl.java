package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.PriceRequestClientResponseRequest;
import ai.lab.inlive.dto.request.PriceRequestCreateRequest;
import ai.lab.inlive.dto.request.PriceRequestUpdateRequest;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.entities.AccSearchRequest;
import ai.lab.inlive.entities.AccommodationUnit;
import ai.lab.inlive.entities.PriceRequest;
import ai.lab.inlive.entities.Reservation;
import ai.lab.inlive.entities.enums.ClientResponseStatus;
import ai.lab.inlive.entities.enums.PriceRequestStatus;
import ai.lab.inlive.entities.enums.ReservationStatus;
import ai.lab.inlive.entities.enums.SearchRequestStatus;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.PriceRequestMapper;
import ai.lab.inlive.repositories.AccSearchRequestRepository;
import ai.lab.inlive.repositories.AccommodationUnitRepository;
import ai.lab.inlive.repositories.PriceRequestRepository;
import ai.lab.inlive.repositories.ReservationRepository;
import ai.lab.inlive.services.PriceRequestService;
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
public class PriceRequestServiceImpl implements PriceRequestService {

    private final PriceRequestRepository priceRequestRepository;
    private final AccSearchRequestRepository accSearchRequestRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final PriceRequestMapper priceRequestMapper;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void createPriceRequest(PriceRequestCreateRequest request) {
        log.info("Creating price request for search request: {} and unit: {}",
                request.getSearchRequestId(), request.getAccommodationUnitId());

        AccSearchRequest searchRequest = accSearchRequestRepository
                .findByIdAndIsDeletedFalse(request.getSearchRequestId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        "Search request not found with ID: " + request.getSearchRequestId()));

        AccommodationUnit unit = accommodationUnitRepository
                .findByIdAndIsDeletedFalse(request.getAccommodationUnitId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + request.getAccommodationUnitId()));

        if (priceRequestRepository.existsBySearchRequestIdAndUnitId(
                request.getSearchRequestId(), request.getAccommodationUnitId())) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "PRICE_REQUEST_ALREADY_EXISTS",
                    "Price request already exists for this search request and unit");
        }

        PriceRequest priceRequest = priceRequestMapper.toEntity(request);
        priceRequest.setSearchRequest(searchRequest);
        priceRequest.setUnit(unit);
        priceRequest.setStatus(PriceRequestStatus.ACCEPTED);
        priceRequest.setClientResponseStatus(ClientResponseStatus.WAITING);

        searchRequest.setStatus(SearchRequestStatus.PRICE_REQUEST_PENDING);
        accSearchRequestRepository.save(searchRequest);

        priceRequestRepository.save(priceRequest);
        log.info("Successfully created price request with ID: {}", priceRequest.getId());
    }

    @Override
    @Transactional
    public void updatePriceRequest(Long priceRequestId, PriceRequestUpdateRequest request) {
        log.info("Updating price request: {} with status: {} and price: {}",
                priceRequestId, request.getStatus(), request.getPrice());

        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(priceRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + priceRequestId));

        priceRequest.setStatus(request.getStatus());
        priceRequest.setPrice(request.getPrice());

        priceRequest.setClientResponseStatus(ClientResponseStatus.WAITING);

        priceRequestRepository.save(priceRequest);
        log.info("Successfully updated price request with ID: {}", priceRequestId);
    }

    @Override
    @Transactional
    public void hidePriceRequest(Long priceRequestId) {
        log.info("Hiding price request with ID: {}", priceRequestId);

        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(priceRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + priceRequestId));

        priceRequest.softDelete();
        priceRequestRepository.save(priceRequest);

        log.info("Successfully hidden price request with ID: {}", priceRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceRequestResponse getPriceRequestById(Long id) {
        log.info("Fetching price request by ID: {}", id);

        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + id));

        return priceRequestMapper.toDto(priceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceRequestResponse> getPriceRequestsByUnitId(Long unitId, Pageable pageable) {
        log.info("Fetching price requests for unit: {}", unitId);

        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        Page<PriceRequest> priceRequests = priceRequestRepository.findActiveByUnitId(unitId, pageable);
        return priceRequests.map(priceRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceRequestResponse> getPriceRequestsBySearchRequestId(Long searchRequestId, Pageable pageable) {
        log.info("Fetching price requests for search request: {}", searchRequestId);

        accSearchRequestRepository.findByIdAndIsDeletedFalse(searchRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        "Search request not found with ID: " + searchRequestId));

        Page<PriceRequest> priceRequests = priceRequestRepository
                .findActiveBySearchRequestId(searchRequestId, pageable);
        return priceRequests.map(priceRequestMapper::toDto);
    }

    @Override
    @Transactional
    public void respondToPriceRequest(Long priceRequestId, PriceRequestClientResponseRequest request, String clientId) {
        log.info("Client {} responding to price request {} with status: {}",
                clientId, priceRequestId, request.getClientResponseStatus());

        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(priceRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + priceRequestId));

        AccSearchRequest searchRequest = priceRequest.getSearchRequest();
        if (!searchRequest.getAuthor().getKeycloakId().equals(clientId)) {
            throw new DbObjectNotFoundException(HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    "You can only respond to price requests for your own search requests");
        }

        if (priceRequest.getClientResponseStatus() != ClientResponseStatus.WAITING) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "ALREADY_RESPONDED",
                    "You have already responded to this price request with status: " +
                            priceRequest.getClientResponseStatus());
        }

        if (request.getClientResponseStatus() != ClientResponseStatus.ACCEPTED &&
                request.getClientResponseStatus() != ClientResponseStatus.REJECTED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_RESPONSE_STATUS",
                    "Client response status must be either ACCEPTED or REJECTED");
        }

        priceRequest.setClientResponseStatus(request.getClientResponseStatus());

        if (request.getClientResponseStatus() == ClientResponseStatus.ACCEPTED) {
            if (reservationRepository.existsByPriceRequestId(priceRequestId)) {
                log.warn("Reservation already exists for price request {}", priceRequestId);
            } else {
                Reservation reservation = new Reservation();
                reservation.setPriceRequest(priceRequest);
                reservation.setUnit(priceRequest.getUnit());
                reservation.setSearchRequest(searchRequest);
                reservation.setApprovedBy(searchRequest.getAuthor());
                reservation.setStatus(ReservationStatus.WAITING_TO_APPROVE);
                reservation.setNeedToPay(false);

                reservationRepository.save(reservation);
                log.info("Automatically created reservation with status WAITING_TO_APPROVE for price request {}", priceRequestId);
            }

            searchRequest.setStatus(SearchRequestStatus.WAIT_TO_RESERVATION);
            accSearchRequestRepository.save(searchRequest);
            log.info("Search request {} status updated to WAIT_TO_RESERVATION", searchRequest.getId());
        }

        priceRequestRepository.save(priceRequest);
        log.info("Successfully processed client response for price request {}", priceRequestId);
    }
}
