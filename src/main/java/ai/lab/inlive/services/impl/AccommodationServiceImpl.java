package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.DictionaryKey;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.AccommodationMapper;
import ai.lab.inlive.mappers.AccSearchRequestMapper;
import ai.lab.inlive.mappers.ImageMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.AccommodationService;
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
    private final AccSearchRequestRepository accSearchRequestRepository;
    private final AccSearchRequestMapper searchRequestMapper;

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

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
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
        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(accommodationId)
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
    public void updateAccommodationPhotos(Long id, List<MultipartFile> photos) {
        log.info("Updating photos for accommodation with ID: {}", id);

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        if (photos != null && !photos.isEmpty()) {
            List<MultipartFile> validPhotos = photos.stream()
                    .filter(Objects::nonNull)
                    .filter(photo -> !photo.isEmpty())
                    .collect(Collectors.toList());

            if (!validPhotos.isEmpty()) {
                List<String> uploadedUrls = Objects.requireNonNull(
                        fileManagerApi.uploadFiles(FILE_MANAGER_ACCOMMODATION_IMAGE_DIR, validPhotos, true).getBody()
                );

                uploadedUrls.forEach(url -> accommodation.getImages().add(mapper.toImage(accommodation, url)));

                log.info("Added {} new photos for accommodation with ID: {}", uploadedUrls.size(), id);
            }
        }

        accommodationRepository.save(accommodation);

        log.info("Successfully updated photos for accommodation with ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAccommodationPhotos(Long id, List<String> photoUrls) {
        log.info("Deleting photos for accommodation with ID: {}", id);

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        if (photoUrls == null || photoUrls.isEmpty()) {
            log.warn("No photo URLs provided for deletion");
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Photo URLs list cannot be empty");
        }

        List<String> urlsToDelete = photoUrls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .toList();

        if (urlsToDelete.isEmpty()) {
            log.warn("No valid photo URLs provided for deletion");
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "No valid photo URLs provided");
        }

        int deletedCount = 0;
        int failedCount = 0;

        var imagesToRemove = accommodation.getImages().stream()
                .filter(image -> urlsToDelete.stream().anyMatch(url ->
                    image.getImageUrl().contains(url) || url.contains(image.getImageUrl())))
                .toList();

        if (imagesToRemove.isEmpty()) {
            log.warn("No matching photos found for deletion");
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "PHOTOS_NOT_FOUND",
                    "No photos found matching the provided URLs");
        }

        for (AccImages image : imagesToRemove) {
            String filename = extractFilename(image.getImageUrl());
            try {
                fileManagerApi.deleteFile(FILE_MANAGER_ACCOMMODATION_IMAGE_DIR, filename);
                log.info("Deleted file from S3: {}", filename);
                accommodation.getImages().remove(image);
                deletedCount++;
            } catch (Exception ex) {
                log.error("Error while deleting file from S3: {}. Error: {}", filename, ex.getMessage());
                failedCount++;
            }
        }

        if (deletedCount > 0) {
            accommodationRepository.save(accommodation);
            log.info("Successfully deleted {} photos for accommodation with ID: {}. Failed: {}",
                    deletedCount, id, failedCount);
        } else {
            throw new DbObjectNotFoundException(HttpStatus.INTERNAL_SERVER_ERROR, "DELETE_FAILED",
                    "Failed to delete any photos from storage");
        }
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

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        accommodation.softDelete();

        accommodationRepository.save(accommodation);

        log.info("Successfully deleted accommodation with ID: {}", id);
    }

    @Override
    @Transactional
    public void approveAccommodation(Long id, String approvedBy) {
        log.info("Approving accommodation with ID: {} by: {}", id, approvedBy);

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        var approver = userRepository.findByKeycloakId(approvedBy)
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

        var accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        var rejecter = userRepository.findByKeycloakId(rejectedBy)
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

        var owner = userRepository.findByKeycloakId(ownerId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + ownerId));

        var accommodations = accommodationRepository.findByOwnerIdWithFilters(owner.getId(), accommodationSearchParams, pageable);

        return accommodations.map(acc -> mapper.toDto(acc, imageMapper));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccSearchRequestResponse> getRelevantRequests(Long accommodationId, Pageable pageable) {
        log.info("Fetching relevant search requests for accommodation: {}", accommodationId);

        accommodationRepository.findByIdAndIsDeletedFalse(accommodationId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND",
                        "Accommodation not found with ID: " + accommodationId));

        Page<AccSearchRequest> requests = accSearchRequestRepository.findRelevantRequestsForAccommodation(accommodationId, pageable);

        log.info("Found {} relevant requests for accommodation {}", requests.getTotalElements(), accommodationId);
        return requests.map(searchRequestMapper::toDto);
    }
}
