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
    public PriceRequestResponse createPriceRequest(PriceRequestCreateRequest request) {
        log.info("Creating price request for search request: {} and unit: {}",
                request.getSearchRequestId(), request.getAccommodationUnitId());

        // Проверяем что search request существует и активен
        AccSearchRequest searchRequest = accSearchRequestRepository
                .findByIdAndIsDeletedFalse(request.getSearchRequestId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        "Search request not found with ID: " + request.getSearchRequestId()));

        // Проверяем что unit существует
        AccommodationUnit unit = accommodationUnitRepository
                .findByIdAndIsDeletedFalse(request.getAccommodationUnitId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + request.getAccommodationUnitId()));

        // Проверяем что для этой пары searchRequest + unit еще нет активной заявки
        if (priceRequestRepository.existsBySearchRequestIdAndUnitId(
                request.getSearchRequestId(), request.getAccommodationUnitId())) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "PRICE_REQUEST_ALREADY_EXISTS",
                    "Price request already exists for this search request and unit");
        }

        // Создаем заявку на цену
        PriceRequest priceRequest = priceRequestMapper.toEntity(request);
        priceRequest.setSearchRequest(searchRequest);
        priceRequest.setUnit(unit);
        priceRequest.setStatus(PriceRequestStatus.ACCEPTED); // По умолчанию принимаем исходную цену
        priceRequest.setClientResponseStatus(ClientResponseStatus.WAITING); // Клиент еще не ответил

        // Обновляем статус search request на PRICE_REQUEST_PENDING
        searchRequest.setStatus(SearchRequestStatus.PRICE_REQUEST_PENDING);
        accSearchRequestRepository.save(searchRequest);

        PriceRequest saved = priceRequestRepository.save(priceRequest);
        log.info("Successfully created price request with ID: {}", saved.getId());

        return priceRequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PriceRequestResponse updatePriceRequest(Long priceRequestId, PriceRequestUpdateRequest request) {
        log.info("Updating price request: {} with status: {} and price: {}",
                priceRequestId, request.getStatus(), request.getPrice());

        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(priceRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + priceRequestId));

        // Обновляем статус и цену
        priceRequest.setStatus(request.getStatus());
        priceRequest.setPrice(request.getPrice());

        // Если статус изменился, сбрасываем ответ клиента на WAITING
        priceRequest.setClientResponseStatus(ClientResponseStatus.WAITING);

        PriceRequest updated = priceRequestRepository.save(priceRequest);
        log.info("Successfully updated price request with ID: {}", priceRequestId);

        return priceRequestMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void hidePriceRequest(Long priceRequestId) {
        log.info("Hiding price request with ID: {}", priceRequestId);

        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(priceRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + priceRequestId));

        // Мягкое удаление (скрытие)
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

        // Проверяем что unit существует
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

        // Проверяем что search request существует
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
    public PriceRequestResponse respondToPriceRequest(Long priceRequestId, PriceRequestClientResponseRequest request, Long clientId) {
        log.info("Client {} responding to price request {} with status: {}",
                clientId, priceRequestId, request.getClientResponseStatus());

        // Находим заявку на цену
        PriceRequest priceRequest = priceRequestRepository.findByIdAndIsDeletedFalse(priceRequestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "PRICE_REQUEST_NOT_FOUND",
                        "Price request not found with ID: " + priceRequestId));

        // Проверяем что клиент является автором search request
        AccSearchRequest searchRequest = priceRequest.getSearchRequest();
        if (!searchRequest.getAuthor().getId().equals(clientId)) {
            throw new DbObjectNotFoundException(HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    "You can only respond to price requests for your own search requests");
        }

        // Проверяем что клиент еще не отвечал на эту заявку
        if (priceRequest.getClientResponseStatus() != ClientResponseStatus.WAITING) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "ALREADY_RESPONDED",
                    "You have already responded to this price request with status: " +
                    priceRequest.getClientResponseStatus());
        }

        // Валидация: можно принять только ACCEPTED или REJECTED
        if (request.getClientResponseStatus() != ClientResponseStatus.ACCEPTED &&
            request.getClientResponseStatus() != ClientResponseStatus.REJECTED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_RESPONSE_STATUS",
                    "Client response status must be either ACCEPTED or REJECTED");
        }

        // Обновляем статус ответа клиента
        priceRequest.setClientResponseStatus(request.getClientResponseStatus());

        // Если клиент принял предложение, создаем бронь автоматически
        if (request.getClientResponseStatus() == ClientResponseStatus.ACCEPTED) {
            // Проверяем что для этой price request еще нет бронирования
            if (reservationRepository.existsByPriceRequestId(priceRequestId)) {
                log.warn("Reservation already exists for price request {}", priceRequestId);
            } else {
                // Автоматически создаем бронирование со статусом WAITING_TO_APPROVE
                Reservation reservation = new Reservation();
                reservation.setPriceRequest(priceRequest);
                reservation.setUnit(priceRequest.getUnit());
                reservation.setSearchRequest(searchRequest);
                reservation.setApprovedBy(searchRequest.getAuthor()); // Клиент, создавший заявку
                reservation.setStatus(ReservationStatus.WAITING_TO_APPROVE); // Ожидает подтверждения от объекта
                reservation.setNeedToPay(false); // На стадии MVP предоплата не требуется

                reservationRepository.save(reservation);
                log.info("Automatically created reservation with status WAITING_TO_APPROVE for price request {}", priceRequestId);
            }

            searchRequest.setStatus(SearchRequestStatus.WAIT_TO_RESERVATION);
            accSearchRequestRepository.save(searchRequest);
            log.info("Search request {} status updated to WAIT_TO_RESERVATION", searchRequest.getId());
        }

        PriceRequest updated = priceRequestRepository.save(priceRequest);
        log.info("Successfully processed client response for price request {}", priceRequestId);

        return priceRequestMapper.toDto(updated);
    }
}
