package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.DictionaryKey;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.AccommodationMapper;
import ai.lab.inlive.mappers.ImageMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.AccommodationService;
import ai.lab.inlivefilemanager.client.api.FileManagerApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_ACCOMMODATION_IMAGE_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper mapper;
    private final ImageMapper imageMapper;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;
    private final FileManagerApi fileManagerApi;
    private final DictionaryRepository dictionaryRepository;
    private final AccDictionaryRepository accDictionaryRepository;

    @Override
    @Transactional
    public void createAccommodation(AccommodationCreateRequest request, String createdBy) {
        log.info("Creating accommodation with name: {}", request.getName());

        var images = new HashSet<AccImages>();
        var accDictionaries = new HashSet<AccDictionary>();

        var city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND", "City not found with ID: " + request.getCityId()));
        var district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DISTRICT_NOT_FOUND", "District not found with ID: " + request.getDistrictId()));
        var owner = userRepository.findByKeycloakId(createdBy)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + createdBy));

        var accommodation = mapper.toEntity(request);

        accommodation.setCity(city);
        accommodation.setDistrict(district);
        accommodation.setOwnerId(owner);
        accommodation.setApproved(null);

        if (request.getServiceDictionaryIds() != null) {
            request.getServiceDictionaryIds()
                    .forEach(serviceDictionaryId -> {
                        var dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(serviceDictionaryId)
                                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + serviceDictionaryId));
                        if (dictionary.getKey() != DictionaryKey.ACC_SERVICE) {
                            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + serviceDictionaryId + " must have key ACC_SERVICE");
                        }
                        accDictionaries.add(mapper.toDictionaryLink(accommodation, dictionary));
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
                        accDictionaries.add(mapper.toDictionaryLink(accommodation, dictionary));
                    });
        }

        accommodation.setDictionaries(accDictionaries);

        if (request.getImages() != null) {
            request.getImages()
                    .forEach(image -> {
                        var fileUrl = Objects.requireNonNull(fileManagerApi.uploadFiles(FILE_MANAGER_ACCOMMODATION_IMAGE_DIR, List.of(image), true).getBody()).getFirst();
                        images.add(mapper.toImage(accommodation, fileUrl));
                    });
        }

        accommodation.setImages(images);

        accommodationRepository.save(accommodation);

        log.info("Successfully created accommodation with ID: {}", accommodation.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationResponse getAccommodationById(Long id) {
        log.info("Fetching accommodation by ID: {}", id);
        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));
        return mapper.toDto(accommodation, imageMapper);
    }

    @Override
    @Transactional
    public Page<AccommodationResponse> searchWithParams(AccommodationSearchParams accommodationSearchParams, Pageable pageable) {
        log.info("Searching accommodations with params: {}", accommodationSearchParams);

        var accommodations = accommodationRepository.findWithFilters(accommodationSearchParams, pageable);

        return accommodations.map(acc -> mapper.toDto(acc, imageMapper));
    }

    @Override
    @Transactional
    public void updateAccommodation(Long id, AccommodationUpdateRequest request) {
        log.info("Updating accommodation with ID: {}", id);

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND", "City not found with ID: " + request.getCityId()));
            accommodation.setCity(city);
        }

        if (request.getDistrictId() != null) {
            District district = districtRepository.findById(request.getDistrictId())
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DISTRICT_NOT_FOUND", "District not found with ID: " + request.getDistrictId()));
            accommodation.setDistrict(district);
        }

        if (request.getAddress() != null) {
            accommodation.setAddress(request.getAddress());
        }

        if (request.getName() != null) {
            accommodation.setName(request.getName());
        }

        if (request.getDescription() != null) {
            accommodation.setDescription(request.getDescription());
        }

        if (request.getRating() != null) {
            accommodation.setRating(request.getRating());
        }

        accommodationRepository.save(accommodation);
        log.info("Successfully updated accommodation with ID: {}", id);
    }

    @Override
    @Transactional
    public void updateDictionaries(Long accommodationId, AccommodationDictionariesUpdateRequest request) {
        log.info("Updating dictionaries for accommodation: {}", accommodationId);
        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(accommodationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND",
                        "Accommodation not found with ID: " + accommodationId));

        if (request.getServiceDictionaryIds() != null) {
            log.info("Updating services for accommodation: {}", accommodationId);
            accDictionaryRepository.deleteByAccommodationAndDictionaryKey(accommodation, DictionaryKey.ACC_SERVICE);
            accDictionaryRepository.flush();

            for (Long dictionaryId : request.getServiceDictionaryIds()) {
                Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(dictionaryId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND",
                                "Dictionary not found with ID: " + dictionaryId));

                if (dictionary.getKey() != DictionaryKey.ACC_SERVICE) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY",
                            "Dictionary ID " + dictionaryId + " must have key ACC_SERVICE");
                }

                AccDictionary link = mapper.toDictionaryLink(accommodation, dictionary);
                accDictionaryRepository.save(link);
            }
            log.info("Successfully updated {} services for accommodation {}", request.getServiceDictionaryIds().size(), accommodationId);
        }

        if (request.getConditionDictionaryIds() != null) {
            log.info("Updating conditions for accommodation: {}", accommodationId);
            accDictionaryRepository.deleteByAccommodationAndDictionaryKey(accommodation, DictionaryKey.ACC_CONDITION);
            accDictionaryRepository.flush();

            for (Long dictionaryId : request.getConditionDictionaryIds()) {
                Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(dictionaryId)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND",
                                "Dictionary not found with ID: " + dictionaryId));

                if (dictionary.getKey() != DictionaryKey.ACC_CONDITION) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY",
                            "Dictionary ID " + dictionaryId + " must have key ACC_CONDITION");
                }

                AccDictionary link = mapper.toDictionaryLink(accommodation, dictionary);
                accDictionaryRepository.save(link);
            }
            log.info("Successfully updated {} conditions for accommodation {}", request.getConditionDictionaryIds().size(), accommodationId);
        }
    }

    @Override
    @Transactional
    public void updateAccommodationPhotos(Long id, List<String> photoUrls) {
        log.info("Updating photos for accommodation with ID: {}", id);

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        Set<String> newUrls = photoUrls == null ? Set.of() : photoUrls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> existingUrls = accommodation.getImages().stream()
                .map(AccImages::getImageUrl)
                .collect(Collectors.toSet());

        Set<String> toRemove = existingUrls.stream()
                .filter(url -> !newUrls.contains(url))
                .collect(Collectors.toSet());

        Set<String> toAdd = newUrls.stream()
                .filter(url -> !existingUrls.contains(url))
                .collect(Collectors.toSet());

        if (!toRemove.isEmpty()) {
            accommodation.getImages().removeIf(img -> toRemove.contains(img.getImageUrl()));
            toRemove.forEach(url -> {
                String filename = extractFilename(url);
                try {
                    ResponseEntity<String> resp = fileManagerApi.deleteFile(FILE_MANAGER_ACCOMMODATION_IMAGE_DIR, filename);
                    if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
                        log.warn("File deletion may have failed for URL: {} (status: {})", url, resp != null ? resp.getStatusCode() : null);
                    }
                } catch (Exception ex) {
                    log.warn("Error while deleting file from file-manager for URL: {}. Continuing DB update. Error: {}", url, ex.getMessage());
                }
            });
        }

        toAdd.forEach(url -> accommodation.getImages().add(mapper.toImage(accommodation, url)));

        accommodationRepository.save(accommodation);

        log.info("Updated photos for accommodation with ID: {}. Added: {}, Removed: {}", id, toAdd.size(), toRemove.size());
    }

    private String extractFilename(String url) {
        if (url == null) return null;
        String noQuery = url.split("\\?")[0];
        int lastSlash = noQuery.lastIndexOf('/');
        return lastSlash >= 0 ? noQuery.substring(lastSlash + 1) : noQuery;
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        log.info("Deleting accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        accommodation.softDelete();

        accommodationRepository.save(accommodation);

        log.info("Successfully deleted accommodation with ID: {}", id);
    }

    @Override
    @Transactional
    public void approveAccommodation(Long id, String approvedBy) {
        log.info("Approving accommodation with ID: {} by: {}", id, approvedBy);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        User approver = userRepository.findByKeycloakId(approvedBy)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + approvedBy));

        accommodation.setApproved(true);
        accommodation.setApprovedBy(approver);

        accommodationRepository.save(accommodation);
        log.info("Successfully approved accommodation with ID: {}", id);
    }

    @Override
    @Transactional
    public void rejectAccommodation(Long id, String rejectedBy) {
        log.info("Rejecting accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        User rejecter = userRepository.findByKeycloakId(rejectedBy)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + rejectedBy));

        accommodation.setApproved(false);
        accommodation.setApprovedBy(rejecter);

        accommodationRepository.save(accommodation);
        log.info("Successfully rejected accommodation with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccommodationResponse> getAccommodationsByOwner(String ownerId, AccommodationSearchParams accommodationSearchParams, Pageable pageable) {
        log.info("Fetching accommodations for owner: {} with pagination", ownerId);

        User owner = userRepository.findByKeycloakId(ownerId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + ownerId));

        var accommodations = accommodationRepository.findByOwnerIdWithFilters(owner.getId(), accommodationSearchParams, pageable);

        return accommodations.map(acc -> mapper.toDto(acc, imageMapper));
    }
}
