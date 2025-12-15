package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.AccSearchRequestCreateRequest;
import ai.lab.inlive.dto.request.AccSearchRequestUpdatePriceRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.DictionaryKey;
import ai.lab.inlive.entities.enums.SearchRequestStatus;
import ai.lab.inlive.entities.enums.UnitType;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.AccSearchRequestMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.AccSearchRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ai.lab.inlive.constants.ValueConstants.ZONE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccSearchRequestServiceImpl implements AccSearchRequestService {

    private final AccSearchRequestRepository accSearchRequestRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final DictionaryRepository dictionaryRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final ReservationRepository reservationRepository;
    private final AccSearchRequestMapper searchRequestMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void createSearchRequest(AccSearchRequestCreateRequest request, String authorId) {
        log.info("Creating search request for user: {}", authorId);

        LocalDateTime checkInDateTime = request.getCheckInDate().atTime(12, 0);
        LocalDateTime checkOutDateTime;

        if (Boolean.TRUE.equals(request.getOneNight())) {
            checkOutDateTime = request.getCheckInDate().plusDays(1).atTime(12, 0);
            log.info("One night stay: checkOut automatically set to {}", checkOutDateTime);
        } else {
            if (request.getCheckOutDate() == null) {
                throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        "CHECKOUT_DATE_REQUIRED",
                        messageSource.getMessage("services.searchRequest.checkoutDateRequired", null, LocaleContextHolder.getLocale()));
            }
            checkOutDateTime = request.getCheckOutDate().atTime(12, 0);
        }

        if (checkOutDateTime.isBefore(checkInDateTime) || checkOutDateTime.isEqual(checkInDateTime)) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_DATES",
                    messageSource.getMessage("services.searchRequest.invalidDates", null, LocaleContextHolder.getLocale()));
        }

        var author = userRepository.findByKeycloakId(authorId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", 
                        messageSource.getMessage("services.searchRequest.userNotFound", 
                                new Object[]{authorId}, LocaleContextHolder.getLocale())));

        List<District> districts = new ArrayList<>();
        for (Long districtId : request.getDistrictIds()) {
            District district = districtRepository.findById(districtId)
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                            "DISTRICT_NOT_FOUND",
                            messageSource.getMessage("services.searchRequest.districtNotFound", 
                                    new Object[]{districtId}, LocaleContextHolder.getLocale())));
            districts.add(district);
        }

        List<Dictionary> services = new ArrayList<>();
        if (request.getServiceDictionaryIds() != null && !request.getServiceDictionaryIds().isEmpty()) {
            for (Long serviceId : request.getServiceDictionaryIds()) {
                Dictionary service = dictionaryRepository.findByIdAndIsDeletedFalse(serviceId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                                "DICTIONARY_NOT_FOUND",
                                messageSource.getMessage("services.searchRequest.dictionaryNotFound", 
                                        new Object[]{serviceId}, LocaleContextHolder.getLocale())));
                if (service.getKey() != DictionaryKey.ACC_SERVICE) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                            "INVALID_DICTIONARY_KEY",
                            messageSource.getMessage("services.searchRequest.invalidDictionaryKey", 
                                    new Object[]{serviceId, "ACC_SERVICE"}, LocaleContextHolder.getLocale()));
                }
                services.add(service);
            }
        }

        List<Dictionary> conditions = new ArrayList<>();
        if (request.getConditionDictionaryIds() != null && !request.getConditionDictionaryIds().isEmpty()) {
            for (Long conditionId : request.getConditionDictionaryIds()) {
                Dictionary condition = dictionaryRepository.findByIdAndIsDeletedFalse(conditionId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                                "DICTIONARY_NOT_FOUND",
                                messageSource.getMessage("services.searchRequest.dictionaryNotFound", 
                                        new Object[]{conditionId}, LocaleContextHolder.getLocale())));
                if (condition.getKey() != DictionaryKey.ACC_CONDITION) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                            "INVALID_DICTIONARY_KEY",
                            messageSource.getMessage("services.searchRequest.invalidDictionaryKey", 
                                    new Object[]{conditionId, "ACC_CONDITION"}, LocaleContextHolder.getLocale()));
                }
                conditions.add(condition);
            }
        }

        String failureReason = checkAvailableUnits(request, districts, services, conditions, checkInDateTime, checkOutDateTime);

        if (failureReason != null) {
            log.warn("No matching accommodation units found: {}", failureReason);
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "NO_MATCHING_UNITS",
                    failureReason);
        }

        AccSearchRequest searchRequest = new AccSearchRequest();
        searchRequest.setAuthor(author);
        searchRequest.setFromDate(checkInDateTime);
        searchRequest.setToDate(checkOutDateTime);
        searchRequest.setOneNight(Boolean.TRUE.equals(request.getOneNight()));
        searchRequest.setPrice(request.getPrice());
        searchRequest.setCountOfPeople(request.getCountOfPeople());
        searchRequest.setFromRating(request.getFromRating());
        searchRequest.setToRating(request.getToRating());
        searchRequest.setStatus(SearchRequestStatus.OPEN_TO_PRICE_REQUEST);

        LocalDateTime expiresAt = calculateExpirationTime(checkInDateTime);
        searchRequest.setExpiresAt(expiresAt);
        log.info("Search request will expire at: {}", expiresAt);

        AccSearchRequest saved = accSearchRequestRepository.save(searchRequest);

        Set<AccSearchRequestUnitType> unitTypes = new HashSet<>();
        for (UnitType unitType : request.getUnitTypes()) {
            AccSearchRequestUnitType requestUnitType = new AccSearchRequestUnitType();
            requestUnitType.setSearchRequest(saved);
            requestUnitType.setUnitType(unitType);
            unitTypes.add(requestUnitType);
        }
        saved.setUnitTypes(unitTypes);

        Set<AccSearchRequestDistrict> requestDistricts = new HashSet<>();
        for (District district : districts) {
            AccSearchRequestDistrict requestDistrict = new AccSearchRequestDistrict();
            requestDistrict.setSearchRequest(saved);
            requestDistrict.setDistrict(district);
            requestDistricts.add(requestDistrict);
        }
        saved.setDistricts(requestDistricts);

        Set<AccSearchRequestDictionary> dictionaries = new HashSet<>();
        for (Dictionary service : services) {
            AccSearchRequestDictionary dict = new AccSearchRequestDictionary();
            dict.setSearchRequest(saved);
            dict.setDictionary(service);
            dictionaries.add(dict);
        }
        for (Dictionary condition : conditions) {
            AccSearchRequestDictionary dict = new AccSearchRequestDictionary();
            dict.setSearchRequest(saved);
            dict.setDictionary(condition);
            dictionaries.add(dict);
        }
        saved.setDictionaries(dictionaries);

        saved = accSearchRequestRepository.save(saved);

        log.info("Successfully created search request with ID: {} for user: {}", saved.getId(), authorId);
    }

    private LocalDateTime calculateExpirationTime(LocalDateTime checkInDate) {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDate today = now.toLocalDate();
        LocalDate checkInDay = checkInDate.toLocalDate();

        if (today.equals(checkInDay)) {
            return now.plusHours(8);
        } else {
            return now.plusHours(24);
        }
    }

    private String checkAvailableUnits(AccSearchRequestCreateRequest request,
                                       List<District> districts,
                                       List<Dictionary> services,
                                       List<Dictionary> conditions,
                                       LocalDateTime checkInDate,
                                       LocalDateTime checkOutDate) {

        Set<Long> districtIds = new HashSet<>();
        String districtNames = districts.stream()
                .map(District::getName)
                .collect(Collectors.joining(", "));

        for (District district : districts) {
            districtIds.add(district.getId());
        }

        List<AccommodationUnit> units = accommodationUnitRepository.findAll();

        if (units.isEmpty()) {
            return messageSource.getMessage("services.searchRequest.noAccommodations", null, LocaleContextHolder.getLocale());
        }

        int totalUnits = 0;
        int failedByType = 0;
        int failedByDistrict = 0;
        int failedByRating = 0;
        int failedByCapacity = 0;
        int failedByServices = 0;
        int failedByConditions = 0;
        int failedByPrice = 0;
        int failedByDates = 0;

        for (AccommodationUnit unit : units) {
            if (unit.getIsDeleted() || !unit.getIsAvailable()) {
                continue;
            }

            Accommodation acc = unit.getAccommodation();
            if (acc.getIsDeleted()) {
                continue;
            }

            totalUnits++;

            if (!request.getUnitTypes().contains(unit.getUnitType())) {
                failedByType++;
                continue;
            }

            if (!districtIds.contains(acc.getDistrict().getId())) {
                failedByDistrict++;
                continue;
            }

            if (request.getFromRating() != null && acc.getRating() < request.getFromRating()) {
                failedByRating++;
                continue;
            }
            if (request.getToRating() != null && acc.getRating() > request.getToRating()) {
                failedByRating++;
                continue;
            }

            if (unit.getCapacity() < request.getCountOfPeople()) {
                failedByCapacity++;
                continue;
            }

            boolean hasAllServices = true;
            if (!services.isEmpty()) {
                Set<Long> unitServiceIds = new HashSet<>();
                for (AccUnitDictionary unitDict : unit.getDictionaries()) {
                    if (unitDict.getDictionary().getKey() == DictionaryKey.ACC_SERVICE) {
                        unitServiceIds.add(unitDict.getDictionary().getId());
                    }
                }
                for (Dictionary service : services) {
                    if (!unitServiceIds.contains(service.getId())) {
                        hasAllServices = false;
                        break;
                    }
                }
            }
            if (!hasAllServices) {
                failedByServices++;
                continue;
            }

            boolean hasAllConditions = true;
            if (!conditions.isEmpty()) {
                Set<Long> unitConditionIds = new HashSet<>();
                for (AccUnitDictionary unitDict : unit.getDictionaries()) {
                    if (unitDict.getDictionary().getKey() == DictionaryKey.ACC_CONDITION) {
                        unitConditionIds.add(unitDict.getDictionary().getId());
                    }
                }
                for (Dictionary condition : conditions) {
                    if (!unitConditionIds.contains(condition.getId())) {
                        hasAllConditions = false;
                        break;
                    }
                }
            }
            if (!hasAllConditions) {
                failedByConditions++;
                continue;
            }

            if (request.getPrice() != null && !unit.getTariffs().isEmpty()) {
                Double minPrice = unit.getTariffs().stream()
                        .map(AccUnitTariffs::getPrice)
                        .min(Double::compareTo)
                        .orElse(null);

                if (minPrice > request.getPrice()) {
                    failedByPrice++;
                    continue;
                }
            }

            if (reservationRepository.isUnitReservedForPeriod(unit.getId(), checkInDate, checkOutDate)) {
                failedByDates++;
                continue;
            }

            return null;
        }

        if (failedByDistrict > 0 && failedByDistrict == totalUnits) {
            return messageSource.getMessage("services.searchRequest.noMatchingDistricts", 
                    new Object[]{districtNames}, LocaleContextHolder.getLocale());
        }

        if (failedByDates > 0) {
            return messageSource.getMessage("services.searchRequest.noAvailableDates", 
                    null, LocaleContextHolder.getLocale());
        }

        if (failedByPrice > 0) {
            return messageSource.getMessage("services.searchRequest.noMatchingPrice", 
                    new Object[]{request.getPrice()}, LocaleContextHolder.getLocale());
        }

        if (failedByCapacity > 0) {
            return messageSource.getMessage("services.searchRequest.noMatchingCapacity", 
                    new Object[]{request.getCountOfPeople()}, LocaleContextHolder.getLocale());
        }

        if (failedByType > 0) {
            String types = request.getUnitTypes().stream()
                    .map(UnitType::name)
                    .collect(Collectors.joining(", "));
            return messageSource.getMessage("services.searchRequest.noMatchingTypes", 
                    new Object[]{types}, LocaleContextHolder.getLocale());
        }

        if (failedByRating > 0) {
            if (request.getFromRating() != null && request.getToRating() != null) {
                return messageSource.getMessage("services.searchRequest.noMatchingRatingRange", 
                        new Object[]{request.getFromRating(), request.getToRating()}, LocaleContextHolder.getLocale());
            } else if (request.getFromRating() != null) {
                return messageSource.getMessage("services.searchRequest.noMatchingRatingFrom", 
                        new Object[]{request.getFromRating()}, LocaleContextHolder.getLocale());
            } else {
                return messageSource.getMessage("services.searchRequest.noMatchingRatingTo", 
                        new Object[]{request.getToRating()}, LocaleContextHolder.getLocale());
            }
        }

        if (failedByServices > 0) {
            String serviceNames = services.stream()
                    .map(Dictionary::getValue)
                    .collect(Collectors.joining(", "));
            return messageSource.getMessage("services.searchRequest.noMatchingServices", 
                    new Object[]{serviceNames}, LocaleContextHolder.getLocale());
        }

        if (failedByConditions > 0) {
            String conditionNames = conditions.stream()
                    .map(Dictionary::getValue)
                    .collect(Collectors.joining(", "));
            return messageSource.getMessage("services.searchRequest.noMatchingConditions", 
                    new Object[]{conditionNames}, LocaleContextHolder.getLocale());
        }

        return messageSource.getMessage("services.searchRequest.noMatchingGeneral", 
                null, LocaleContextHolder.getLocale());
    }

    @Override
    @Transactional(readOnly = true)
    public AccSearchRequestResponse getSearchRequestById(Long id) {
        log.info("Fetching search request by ID: {}", id);
        var searchRequest = accSearchRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        messageSource.getMessage("services.searchRequest.notFound",
                                new Object[]{id}, LocaleContextHolder.getLocale())));
        return searchRequestMapper.toDto(searchRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccSearchRequestResponse> getMySearchRequests(String authorId, Pageable pageable) {
        log.info("Fetching search requests for user: {}", authorId);

        var author = userRepository.findByKeycloakId(authorId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        messageSource.getMessage("services.searchRequest.userNotFound",
                                new Object[]{authorId}, LocaleContextHolder.getLocale())));

        Page<AccSearchRequest> requests = accSearchRequestRepository.findAllByAuthor_IdAndIsDeletedFalse(author.getId(), pageable);

        return requests.map(searchRequestMapper::toDto);
    }

    @Override
    @Transactional
    public void updateSearchRequestPrice(Long id, AccSearchRequestUpdatePriceRequest request, String authorId) {
        log.info("Updating price for search request ID: {} by user: {}", id, authorId);

        var searchRequest = accSearchRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        messageSource.getMessage("services.searchRequest.notFound",
                                new Object[]{id}, LocaleContextHolder.getLocale())));

        if (!searchRequest.getAuthor().getKeycloakId().equals(authorId)) {
            throw new DbObjectNotFoundException(HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    messageSource.getMessage("services.searchRequest.accessDenied", null, LocaleContextHolder.getLocale()));
        }

        if (searchRequest.getStatus() == SearchRequestStatus.CANCELLED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_CANCELLED",
                    messageSource.getMessage("services.searchRequest.cancelled", null, LocaleContextHolder.getLocale()));
        }

        if (searchRequest.getStatus() == SearchRequestStatus.FINISHED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_FINISHED",
                    messageSource.getMessage("services.searchRequest.finished", null, LocaleContextHolder.getLocale()));
        }

        searchRequest.setPrice(request.getPrice());
        accSearchRequestRepository.save(searchRequest);

        log.info("Successfully updated price for search request ID: {}", id);
    }

    @Override
    @Transactional
    public void cancelSearchRequest(Long id, String authorId) {
        log.info("Cancelling search request ID: {} by user: {}", id, authorId);

        AccSearchRequest searchRequest = accSearchRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        "Search request not found with ID: " + id));

        if (!searchRequest.getAuthor().getKeycloakId().equals(authorId)) {
            throw new DbObjectNotFoundException(HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    messageSource.getMessage("services.searchRequest.accessDenied", null, LocaleContextHolder.getLocale()));
        }

        if (searchRequest.getStatus() == SearchRequestStatus.CANCELLED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_ALREADY_CANCELLED",
                    messageSource.getMessage("services.searchRequest.alreadyCancelled", null, LocaleContextHolder.getLocale()));
        }

        if (searchRequest.getStatus() == SearchRequestStatus.FINISHED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_ALREADY_FINISHED",
                    messageSource.getMessage("services.searchRequest.alreadyFinished", null, LocaleContextHolder.getLocale()));
        }

        searchRequest.setStatus(SearchRequestStatus.CANCELLED);
        searchRequest.softDelete();
        accSearchRequestRepository.save(searchRequest);

        log.info("Successfully cancelled search request ID: {}", id);
    }
}
