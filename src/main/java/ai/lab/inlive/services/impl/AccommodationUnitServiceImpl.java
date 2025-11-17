package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.dto.request.AccUnitDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.entities.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    @Override
    @Transactional
    public void createUnit(AccommodationUnitCreateRequest request) {
        log.info("Creating accommodation unit for accommodation: {}", request.getAccommodationId());

        var images = new HashSet<AccUnitImages>();
        var unitDictionaries = new HashSet<AccUnitDictionary>();

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(request.getAccommodationId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + request.getAccommodationId()));

        var unit = unitMapper.toEntity(request);
        unit.setAccommodation(accommodation);

        if (request.getServiceDictionaryIds() != null) {
            request.getServiceDictionaryIds()
                    .forEach(serviceDictionaryId -> {
                        var dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(serviceDictionaryId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + serviceDictionaryId));
                        if (dictionary.getKey() != DictionaryKey.ACC_SERVICE) {
                            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + serviceDictionaryId + " must have key ACC_SERVICE");
                        }
                        unitDictionaries.add(unitMapper.toDictionaryLink(accommodation, unit, dictionary));
                    });
        }

        if (request.getConditionDictionaryIds() != null) {
            request.getConditionDictionaryIds()
                    .forEach(conditionDictionaryId -> {
                        var dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(conditionDictionaryId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + conditionDictionaryId));
                        if (dictionary.getKey() != DictionaryKey.ACC_CONDITION) {
                            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + conditionDictionaryId + " must have key ACC_CONDITION");
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
    public AccUnitTariffResponse addTariff(Long unitId, AccUnitTariffCreateRequest request) {
        log.info("Adding tariff for unit: {}", unitId);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + unitId));

        Dictionary rangeType = dictionaryRepository.findByIdAndIsDeletedFalse(request.getRangeTypeId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + request.getRangeTypeId()));

        if (rangeType.getKey() != DictionaryKey.RANGE_TYPE) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + request.getRangeTypeId() + " must have key RANGE_TYPE");
        }

        AccUnitTariffs tariff = unitMapper.toEntity(request);
        tariff.setAccommodation(unit.getAccommodation());
        tariff.setUnit(unit);
        tariff.setRangeType(rangeType);

        AccUnitTariffs saved = accUnitTariffsRepository.save(tariff);

        AccUnitTariffResponse response = unitMapper.toDto(saved);
        log.info("Created tariff {} for unit {}", saved.getId(), unitId);
        return response;
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
                                "Dictionary not found with ID: " + dictionaryId));

                if (dictionary.getKey() != DictionaryKey.ACC_SERVICE) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY",
                            "Dictionary ID " + dictionaryId + " must have key ACC_SERVICE");
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
                                "Dictionary not found with ID: " + dictionaryId));

                if (dictionary.getKey() != DictionaryKey.ACC_CONDITION) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY",
                            "Dictionary ID " + dictionaryId + " must have key ACC_CONDITION");
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
    public void deleteAccommodationUnitPhoto(Long id, String photoUrl) {
        log.info("Deleting photo for accommodation unit with ID: {}, photoUrl: {}", id, photoUrl);

        var unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + id));

        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_PHOTO_URL", "Photo URL cannot be empty");
        }

        String urlToDelete = photoUrl.trim();

        AccUnitImages imageToRemove = unit.getImages().stream()
                .filter(image -> image.getImageUrl().contains(urlToDelete) || urlToDelete.contains(image.getImageUrl()))
                .findFirst()
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "PHOTO_NOT_FOUND",
                        "Photo not found with URL: " + urlToDelete));

        String filename = extractFilename(imageToRemove.getImageUrl());

        try {
            fileManagerApi.deleteFile(FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR, filename);
            log.info("Deleted file from S3: {}", filename);
        } catch (Exception ex) {
            log.error("Error while deleting file from S3: {}. Error: {}", filename, ex.getMessage());
            throw new DbObjectNotFoundException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_DELETE_ERROR",
                    "Failed to delete file from storage: " + ex.getMessage());
        }

        unit.getImages().remove(imageToRemove);
        accommodationUnitRepository.save(unit);

        log.info("Successfully deleted photo for accommodation unit with ID: {}", id);
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
        return requests.map(searchRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceRequestResponse> getUnitPriceRequests(Long unitId, Pageable pageable) {
        log.info("Fetching price requests for unit: {}", unitId);

        // Проверяем что unit существует
        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        // Получаем заявки на цену
        Page<PriceRequest> priceRequests = priceRequestRepository.findActiveByUnitId(unitId, pageable);

        log.info("Found {} price requests for unit {}", priceRequests.getTotalElements(), unitId);
        return priceRequests.map(priceRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUnitPendingReservations(Long unitId, Pageable pageable) {
        log.info("Fetching pending reservations for unit: {}", unitId);

        // Проверяем что unit существует
        accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND",
                        "Accommodation Unit not found with ID: " + unitId));

        // Получаем ожидающие бронирования
        Page<Reservation> reservations = reservationRepository.findPendingByUnitId(unitId, pageable);

        log.info("Found {} pending reservations for unit {}", reservations.getTotalElements(), unitId);
        return reservations.map(reservationMapper::toDto);
    }
}
