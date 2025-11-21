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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccSearchRequestServiceImpl implements AccSearchRequestService {

    private final AccSearchRequestRepository accSearchRequestRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final DictionaryRepository dictionaryRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final AccSearchRequestMapper searchRequestMapper;

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
                        "Check-out date is required when oneNight is false or not specified");
            }
            checkOutDateTime = request.getCheckOutDate().atTime(12, 0);
        }

        if (checkOutDateTime.isBefore(checkInDateTime) || checkOutDateTime.isEqual(checkInDateTime)) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "INVALID_DATES",
                    "Check-out date must be after check-in date");
        }

        var author = userRepository.findByKeycloakId(authorId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + authorId));

        List<District> districts = new ArrayList<>();
        for (Long districtId : request.getDistrictIds()) {
            District district = districtRepository.findById(districtId)
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                            "DISTRICT_NOT_FOUND",
                            "District not found with ID: " + districtId));
            districts.add(district);
        }

        List<Dictionary> services = new ArrayList<>();
        if (request.getServiceDictionaryIds() != null && !request.getServiceDictionaryIds().isEmpty()) {
            for (Long serviceId : request.getServiceDictionaryIds()) {
                Dictionary service = dictionaryRepository.findByIdAndIsDeletedFalse(serviceId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                                "DICTIONARY_NOT_FOUND",
                                "Service dictionary not found with ID: " + serviceId));
                if (service.getKey() != DictionaryKey.ACC_SERVICE) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                            "INVALID_DICTIONARY_KEY",
                            "Dictionary ID " + serviceId + " must have key ACC_SERVICE");
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
                                "Condition dictionary not found with ID: " + conditionId));
                if (condition.getKey() != DictionaryKey.ACC_CONDITION) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                            "INVALID_DICTIONARY_KEY",
                            "Dictionary ID " + conditionId + " must have key ACC_CONDITION");
                }
                conditions.add(condition);
            }
        }

        boolean hasMatchingUnits = checkAvailableUnits(request, districts, services, conditions);

        if (!hasMatchingUnits) {
            log.warn("No matching accommodation units found for search request");
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "NO_MATCHING_UNITS",
                    "К сожалению, нет подходящих вариантов по вашим параметрам. Пожалуйста, пересмотрите запрошенные параметры: " +
                    "проверьте район, тип недвижимости, необходимые услуги и условия проживания.");
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

    private boolean checkAvailableUnits(AccSearchRequestCreateRequest request,
                                       List<District> districts,
                                       List<Dictionary> services,
                                       List<Dictionary> conditions) {

        Set<Long> districtIds = new HashSet<>();
        for (District district : districts) {
            districtIds.add(district.getId());
        }

        List<AccommodationUnit> units = accommodationUnitRepository.findAll();

        for (AccommodationUnit unit : units) {
            if (unit.getIsDeleted() || !unit.getIsAvailable()) {
                continue;
            }

            Accommodation acc = unit.getAccommodation();
            if (acc.getIsDeleted()) {
                continue;
            }

            if (!request.getUnitTypes().contains(unit.getUnitType())) {
                continue;
            }

            if (!districtIds.contains(acc.getDistrict().getId())) {
                continue;
            }

            if (request.getFromRating() != null && acc.getRating() < request.getFromRating()) {
                continue;
            }
            if (request.getToRating() != null && acc.getRating() > request.getToRating()) {
                continue;
            }

            if (unit.getCapacity() < request.getCountOfPeople()) {
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
                continue;
            }

            return true;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public AccSearchRequestResponse getSearchRequestById(Long id) {
        log.info("Fetching search request by ID: {}", id);
        var searchRequest = accSearchRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "SEARCH_REQUEST_NOT_FOUND",
                        "Search request not found with ID: " + id));
        return searchRequestMapper.toDto(searchRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccSearchRequestResponse> getMySearchRequests(String authorId, Pageable pageable) {
        log.info("Fetching search requests for user: {}", authorId);

        var author = userRepository.findByKeycloakId(authorId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + authorId));

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
                        "Search request not found with ID: " + id));

        if (!searchRequest.getAuthor().getKeycloakId().equals(authorId)) {
            throw new DbObjectNotFoundException(HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    "You can only update your own search requests");
        }

        if (searchRequest.getStatus() == SearchRequestStatus.CANCELLED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_CANCELLED",
                    "Cannot update price of a cancelled search request");
        }

        if (searchRequest.getStatus() == SearchRequestStatus.FINISHED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_FINISHED",
                    "Cannot update price of a finished search request");
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
                    "You can only cancel your own search requests");
        }

        if (searchRequest.getStatus() == SearchRequestStatus.CANCELLED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_ALREADY_CANCELLED",
                    "Search request is already cancelled");
        }

        if (searchRequest.getStatus() == SearchRequestStatus.FINISHED) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                    "SEARCH_REQUEST_ALREADY_FINISHED",
                    "Cannot cancel a finished search request");
        }

        searchRequest.setStatus(SearchRequestStatus.CANCELLED);
        searchRequest.softDelete();
        accSearchRequestRepository.save(searchRequest);

        log.info("Successfully cancelled search request ID: {}", id);
    }
}
