package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.dto.request.AccUnitDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.Dictionary;
import ai.lab.inlive.entities.enums.DictionaryKey;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.AccommodationUnitMapper;
import ai.lab.inlive.mappers.AccSearchRequestMapper;
import ai.lab.inlive.mappers.ImageMapper;
import ai.lab.inlive.mappers.PriceRequestMapper;
import ai.lab.inlive.mappers.ReservationMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.AccommodationUnitService;
import ai.lab.inlivefilemanager.client.api.FileManagerApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationUnitServiceImpl implements AccommodationUnitService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final AccUnitTariffsRepository accUnitTariffsRepository;
    private final DictionaryRepository dictionaryRepository;
    private final AccUnitDictionaryRepository accUnitDictionaryRepository;
    private final AccSearchRequestRepository accSearchRequestRepository;
    private final PriceRequestRepository priceRequestRepository;
    private final ReservationRepository reservationRepository;
    private final AccommodationUnitMapper unitMapper;
    private final ImageMapper imageMapper;
    private final AccSearchRequestMapper searchRequestMapper;
    private final PriceRequestMapper priceRequestMapper;
    private final ReservationMapper reservationMapper;
    private final FileManagerApi fileManagerApi;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void createUnit(AccommodationUnitCreateRequest request) {
        log.info("Creating accommodation unit for accommodation: {}", request.getAccommodationId());

        var images = new HashSet<AccUnitImages>();
        var unitDictionaries = new HashSet<AccUnitDictionary>();

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(request.getAccommodationId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", 
                        messageSource.getMessage("services.accommodationUnit.accommodationNotFound", 
                                new Object[]{request.getAccommodationId()}, LocaleContextHolder.getLocale())));

        var unit = unitMapper.toEntity(request);
        unit.setAccommodation(accommodation);

        if (request.getServiceDictionaryIds() != null) {
            request.getServiceDictionaryIds()
                    .forEach(serviceDictionaryId -> {
                        var dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(serviceDictionaryId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", 
                                        messageSource.getMessage("services.accommodationUnit.dictionaryNotFound", 
                                                new Object[]{serviceDictionaryId}, LocaleContextHolder.getLocale())));
                        if (dictionary.getKey() != DictionaryKey.ACC_SERVICE) {
                            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", 
                                    messageSource.getMessage("services.accommodationUnit.invalidDictionaryKey", 
                                            new Object[]{serviceDictionaryId, "ACC_SERVICE"}, LocaleContextHolder.getLocale()));
                        }
                        unitDictionaries.add(unitMapper.toDictionaryLink(accommodation, unit, dictionary));
                    });
        }

        if (request.getConditionDictionaryIds() != null) {
            request.getConditionDictionaryIds()
                    .forEach(conditionDictionaryId -> {
                        var dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(conditionDictionaryId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", 
                                        messageSource.getMessage("services.accommodationUnit.dictionaryNotFound", 
                                                new Object[]{conditionDictionaryId}, LocaleContextHolder.getLocale())));
                        if (dictionary.getKey() != DictionaryKey.ACC_CONDITION) {
                            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", 
                                    messageSource.getMessage("services.accommodationUnit.invalidDictionaryKey", 
                                            new Object[]{conditionDictionaryId, "ACC_CONDITION"}, LocaleContextHolder.getLocale()));
                        }
                        unitDictionaries.add(unitMapper.toDictionaryLink(accommodation, unit, dictionary));
                    });
        }

        unit.setDictionaries(unitDictionaries);

        var savedUnit = accommodationUnitRepository.save(unit);

        if (request.getImages() != null) {
            request.getImages()
                    .forEach(image -> {
                        var fileUrl = Objects.requireNonNull(fileManagerApi.uploadFiles(FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR, List.of(image), true).getBody()).getFirst();
                        images.add(unitMapper.toImage(accommodation, savedUnit, fileUrl));
                    });
        }

        unit.setImages(images);

        accommodationUnitRepository.save(unit);

        log.info("Successfully created accommodation unit with ID: {}", unit.getId());
    }

    @Override
    @Transactional
    public void addTariff(Long unitId, AccUnitTariffCreateRequest request) {
        log.info("Adding tariff for unit: {}", unitId);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", 
                        messageSource.getMessage("services.accommodationUnit.notFound", 
                                new Object[]{unitId}, LocaleContextHolder.getLocale())));

        Dictionary rangeType = dictionaryRepository.findByIdAndIsDeletedFalse(request.getRangeTypeId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", 
                        messageSource.getMessage("services.accommodationUnit.dictionaryNotFound", 
                                new Object[]{request.getRangeTypeId()}, LocaleContextHolder.getLocale())));

        if (rangeType.getKey() != DictionaryKey.RANGE_TYPE) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", 
                    messageSource.getMessage("services.accommodationUnit.invalidDictionaryKey", 
                            new Object[]{request.getRangeTypeId(), "RANGE_TYPE"}, LocaleContextHolder.getLocale()));
        }

        AccUnitTariffs tariff = unitMapper.toEntity(request);
        tariff.setAccommodation(unit.getAccommodation());
        tariff.setUnit(unit);
        tariff.setRangeType(rangeType);

        accUnitTariffsRepository.save(tariff);
        log.info("Created tariff {} for unit {}", tariff.getId(), unitId);
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationUnitResponse getUnitById(Long id) {
        log.info("Fetching accommodation unit by ID: {}", id);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + id));
        return unitMapper.toDto(unit, imageMapper);
    }

    @Override
    public Page<AccommodationUnitResponse> searchWithParams(AccommodationUnitSearchParams params, Pageable pageable) {
        log.info("Searching accommodation units with params: {}", params);
        var page = accommodationUnitRepository.findWithFilters(params, pageable);
        return page.map(unit -> unitMapper.toDto(unit, imageMapper));
    }

    @Override
    @Transactional
    public void deleteUnit(Long id) {
        log.info("Deleting accommodation unit with ID: {}", id);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + id));
        unit.softDelete();
        accommodationUnitRepository.save(unit);
        log.info("Successfully deleted accommodation unit with ID: {}", id);
    }

    @Override
    @Transactional
    public void updateUnit(Long id, AccommodationUnitUpdateRequest request) {
        log.info("Updating accommodation unit with ID: {}", id);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + id));

        if (request.getUnitType() != null) {
            unit.setUnitType(request.getUnitType());
        }
        if (request.getName() != null) {
            unit.setName(request.getName());
        }
        if (request.getDescription() != null) {
            unit.setDescription(request.getDescription());
        }
        if (request.getCapacity() != null) {
            unit.setCapacity(request.getCapacity());
        }
        if (request.getArea() != null) {
            unit.setArea(request.getArea());
        }
        if (request.getFloor() != null) {
            unit.setFloor(request.getFloor());
        }
        if (request.getIsAvailable() != null) {
            unit.setIsAvailable(request.getIsAvailable());
        }

        accommodationUnitRepository.save(unit);
        log.info("Successfully updated accommodation unit with ID: {}", id);
    }

    @Override
    @Transactional
    public void updateDictionaries(Long unitId, AccUnitDictionariesUpdateRequest request) {
        log.info("Updating dictionaries for unit: {}", unitId);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        if (request.getServiceDictionaryIds() != null) {
            log.info("Updating services for unit: {}", unitId);
            accUnitDictionaryRepository.deleteByUnitAndDictionaryKey(unit, DictionaryKey.ACC_SERVICE);
            accUnitDictionaryRepository.flush();

            for (Long dictionaryId : request.getServiceDictionaryIds()) {
                Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(dictionaryId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND",
                                messageSource.getMessage("services.accommodationUnit.dictionaryNotFound", 
                                        new Object[]{dictionaryId}, LocaleContextHolder.getLocale())));

                if (dictionary.getKey() != DictionaryKey.ACC_SERVICE) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY",
                            messageSource.getMessage("services.accommodationUnit.invalidDictionaryKey", 
                                    new Object[]{dictionaryId, "ACC_SERVICE"}, LocaleContextHolder.getLocale()));
                }

                AccUnitDictionary link = unitMapper.toDictionaryLink(unit.getAccommodation(), unit, dictionary);
                accUnitDictionaryRepository.save(link);
            }
            log.info("Successfully updated {} services for unit {}", request.getServiceDictionaryIds().size(), unitId);
        }

        if (request.getConditionDictionaryIds() != null) {
            log.info("Updating conditions for unit: {}", unitId);
            accUnitDictionaryRepository.deleteByUnitAndDictionaryKey(unit, DictionaryKey.ACC_CONDITION);
            accUnitDictionaryRepository.flush();

            for (Long dictionaryId : request.getConditionDictionaryIds()) {
                Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(dictionaryId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND",
                                messageSource.getMessage("services.accommodationUnit.dictionaryNotFound", 
                                        new Object[]{dictionaryId}, LocaleContextHolder.getLocale())));

                if (dictionary.getKey() != DictionaryKey.ACC_CONDITION) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY",
                            messageSource.getMessage("services.accommodationUnit.invalidDictionaryKey", 
                                    new Object[]{dictionaryId, "ACC_CONDITION"}, LocaleContextHolder.getLocale()));
                }

                AccUnitDictionary link = unitMapper.toDictionaryLink(unit.getAccommodation(), unit, dictionary);
                accUnitDictionaryRepository.save(link);
            }
            log.info("Successfully updated {} conditions for unit {}", request.getConditionDictionaryIds().size(), unitId);
        }
    }

    @Override
    @Transactional
    public void updateAccommodationUnitPhotos(Long id, List<MultipartFile> photos) {
        log.info("Updating photos for accommodation unit with ID: {}", id);

        var unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + id));

        if (photos != null && !photos.isEmpty()) {
            List<MultipartFile> validPhotos = photos.stream()
                    .filter(Objects::nonNull)
                    .filter(photo -> !photo.isEmpty())
                    .collect(Collectors.toList());

            if (!validPhotos.isEmpty()) {
                List<String> uploadedUrls = Objects.requireNonNull(
                        fileManagerApi.uploadFiles(FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR, validPhotos, true).getBody()
                );

                uploadedUrls.forEach(url -> unit.getImages().add(unitMapper.toImage(unit.getAccommodation(), unit, url)));

                log.info("Added {} new photos for accommodation unit with ID: {}", uploadedUrls.size(), id);
            }
        }

        accommodationUnitRepository.save(unit);

        log.info("Successfully updated photos for accommodation unit with ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAccommodationUnitPhotos(Long id, List<String> photoUrls) {
        log.info("Deleting photos for accommodation unit with ID: {}", id);

        var unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + id));

        if (photoUrls == null || photoUrls.isEmpty()) {
            log.warn("No photo URLs provided for deletion");
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", 
                    messageSource.getMessage("services.accommodationUnit.photoUrlsEmpty", null, LocaleContextHolder.getLocale()));
        }

        List<String> urlsToDelete = photoUrls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .toList();

        if (urlsToDelete.isEmpty()) {
            log.warn("No valid photo URLs provided for deletion");
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", 
                    messageSource.getMessage("services.accommodationUnit.noValidPhotoUrls", null, LocaleContextHolder.getLocale()));
        }

        int deletedCount = 0;
        int failedCount = 0;

        var imagesToRemove = unit.getImages().stream()
                .filter(image -> urlsToDelete.stream().anyMatch(url ->
                    image.getImageUrl().contains(url) || url.contains(image.getImageUrl())))
                .toList();

        if (imagesToRemove.isEmpty()) {
            log.warn("No matching photos found for deletion");
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "PHOTOS_NOT_FOUND",
                    messageSource.getMessage("services.accommodationUnit.photosNotFound", null, LocaleContextHolder.getLocale()));
        }

        for (AccUnitImages image : imagesToRemove) {
            String filename = extractFilename(image.getImageUrl());
            try {
                fileManagerApi.deleteFile(FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR, filename);
                log.info("Deleted file from S3: {}", filename);
                unit.getImages().remove(image);
                deletedCount++;
            } catch (Exception ex) {
                log.error("Error while deleting file from S3: {}. Error: {}", filename, ex.getMessage());
                failedCount++;
            }
        }

        if (deletedCount > 0) {
            accommodationUnitRepository.save(unit);
            log.info("Successfully deleted {} photos for accommodation unit with ID: {}. Failed: {}",
                    deletedCount, id, failedCount);
        } else {
            throw new DbObjectNotFoundException(HttpStatus.INTERNAL_SERVER_ERROR, "DELETE_FAILED",
                    messageSource.getMessage("services.accommodationUnit.deleteFailed", null, LocaleContextHolder.getLocale()));
        }
    }

    private String extractFilename(String url) {
        if (url == null) return null;
        String noQuery = url.split("\\?")[0];
        int lastSlash = noQuery.lastIndexOf('/');
        return lastSlash >= 0 ? noQuery.substring(lastSlash + 1) : noQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccSearchRequestResponse> getRelevantRequests(Long unitId, Pageable pageable) {
        log.info("Fetching relevant search requests for unit: {}", unitId);

        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        Page<AccSearchRequest> requests = accSearchRequestRepository.findRelevantRequestsForUnit(unitId, pageable);

        log.info("Found {} relevant requests for unit {}", requests.getTotalElements(), unitId);

        if (!requests.isEmpty()) {
            List<Long> ids = requests.getContent().stream()
                    .map(AccSearchRequest::getId)
                    .toList();

            List<AccSearchRequest> fullRequests = accSearchRequestRepository.findAllByIdInWithFetchJoin(ids);

            Map<Long, AccSearchRequest> requestMap = fullRequests.stream()
                    .collect(Collectors.toMap(AccSearchRequest::getId, r -> r));

            List<AccSearchRequestResponse> responses = requests.getContent().stream()
                    .map(req -> searchRequestMapper.toDto(requestMap.get(req.getId())))
                    .toList();

            return new PageImpl<>(responses, pageable, requests.getTotalElements());
        }

        return requests.map(searchRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceRequestResponse> getUnitPriceRequests(Long unitId, Pageable pageable) {
        log.info("Fetching price requests for unit: {}", unitId);

        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        Page<PriceRequest> priceRequests = priceRequestRepository.findActiveByUnitId(unitId, pageable);

        log.info("Found {} price requests for unit {}", priceRequests.getTotalElements(), unitId);
        return priceRequests.map(priceRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUnitPendingReservations(Long unitId, Pageable pageable) {
        log.info("Fetching pending reservations for unit: {}", unitId);

        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        Page<Reservation> reservations = reservationRepository.findPendingByUnitId(unitId, pageable);

        log.info("Found {} pending reservations for unit {}", reservations.getTotalElements(), unitId);
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationUnitResponse> getUnitsByAccommodationAndRequest(Long accommodationId, Long requestId) {
        log.info("Fetching relevant units for accommodation: {} and request: {}", accommodationId, requestId);

        accommodationRepository.findByIdAndIsDeletedFalse(accommodationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND",
                        messageSource.getMessage("services.accommodationUnit.accommodationNotFound", 
                                new Object[]{accommodationId}, LocaleContextHolder.getLocale())));

        AccSearchRequest searchRequest = accSearchRequestRepository.findByIdAndIsDeletedFalse(requestId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "SEARCH_REQUEST_NOT_FOUND",
                        messageSource.getMessage("services.accommodationUnit.searchRequestNotFound", 
                                new Object[]{requestId}, LocaleContextHolder.getLocale())));

        List<AccommodationUnit> allUnits = accommodationUnitRepository.findByAccommodationIdAndIsDeletedFalse(accommodationId);

        List<AccommodationUnit> relevantUnits = allUnits.stream()
                .filter(unit -> isUnitRelevantForRequest(unit, searchRequest))
                .toList();

        log.info("Found {} relevant units out of {} total units for accommodation {} and request {}",
                relevantUnits.size(), allUnits.size(), accommodationId, requestId);

        return relevantUnits.stream()
                .map(unit -> unitMapper.toDto(unit, imageMapper))
                .collect(Collectors.toList());
    }

    private boolean isUnitRelevantForRequest(AccommodationUnit unit, AccSearchRequest searchRequest) {
        Accommodation accommodation = unit.getAccommodation();

        // 1. Проверка типа недвижимости
        boolean hasMatchingType = searchRequest.getUnitTypes().stream()
                .anyMatch(requestType -> requestType.getUnitType() == unit.getUnitType());
        if (!hasMatchingType) {
            return false;
        }

        // 2. Проверка рейтинга accommodation
        if (searchRequest.getFromRating() != null && accommodation.getRating() < searchRequest.getFromRating()) {
            return false;
        }
        if (searchRequest.getToRating() != null && accommodation.getRating() > searchRequest.getToRating()) {
            return false;
        }

        // 3. Проверка района
        boolean hasMatchingDistrict = searchRequest.getDistricts().stream()
                .anyMatch(requestDistrict -> requestDistrict.getDistrict().getId().equals(accommodation.getDistrict().getId()));
        if (!hasMatchingDistrict) {
            return false;
        }

        // 4. Проверка услуг (SERVICES) - все услуги из заявки должны быть у unit
        Set<Long> unitServiceIds = unit.getDictionaries().stream()
                .filter(d -> d.getDictionary().getKey() == DictionaryKey.ACC_SERVICE)
                .map(d -> d.getDictionary().getId())
                .collect(Collectors.toSet());

        Set<Long> requestServiceIds = searchRequest.getDictionaries().stream()
                .filter(d -> d.getDictionary().getKey() == DictionaryKey.ACC_SERVICE)
                .map(d -> d.getDictionary().getId())
                .collect(Collectors.toSet());

        if (!unitServiceIds.containsAll(requestServiceIds)) {
            return false;
        }

        // 5. Проверка условий (CONDITIONS) - все условия из заявки должны быть у unit
        Set<Long> unitConditionIds = unit.getDictionaries().stream()
                .filter(d -> d.getDictionary().getKey() == DictionaryKey.ACC_CONDITION)
                .map(d -> d.getDictionary().getId())
                .collect(Collectors.toSet());

        Set<Long> requestConditionIds = searchRequest.getDictionaries().stream()
                .filter(d -> d.getDictionary().getKey() == DictionaryKey.ACC_CONDITION)
                .map(d -> d.getDictionary().getId())
                .collect(Collectors.toSet());

        return unitConditionIds.containsAll(requestConditionIds);
    }
}
