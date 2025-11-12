package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.entities.AccImages;
import ai.lab.inlive.entities.Accommodation;
import ai.lab.inlive.entities.City;
import ai.lab.inlive.entities.District;
import ai.lab.inlive.entities.User;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.AccommodationMapper;
import ai.lab.inlive.repositories.AccommodationRepository;
import ai.lab.inlive.repositories.CityRepository;
import ai.lab.inlive.repositories.DistrictRepository;
import ai.lab.inlive.repositories.UserRepository;
import ai.lab.inlive.services.AccommodationService;
import ai.lab.inlivefilemanager.client.api.FileManagerApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_IMAGE_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper mapper;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;
    private final FileManagerApi fileManagerApi;

    @Override
    @Transactional
    public void createAccommodation(AccommodationCreateRequest request, String createdBy) {
        log.info("Creating accommodation with name: {}", request.getName());

        var images = new HashSet<AccImages>();

        var city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND", "City not found with ID: " + request.getCityId()));
        var district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DISTRICT_NOT_FOUND", "District not found with ID: " + request.getDistrictId()));
        var owner = userRepository.findByKeycloakId(createdBy)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with Keycloak ID: " + createdBy));

        var accommodation = mapper.toEntity(request);

        accommodation.setCity(city);
        accommodation.setDistrict(district);
        accommodation.setRating(request.getRating());
        accommodation.setOwnerId(owner);
        accommodation.setApproved(null);

        if (request.getImages() != null) {
            request.getImages()
                    .forEach(image -> {
                        var fileUrl = Objects.requireNonNull(fileManagerApi.uploadFiles(FILE_MANAGER_IMAGE_DIR, List.of(image), true).getBody()).getFirst();
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
        return mapper.toDto(accommodation);
    }

    @Override
    @Transactional
    public Page<AccommodationResponse> searchWithParams(AccommodationSearchParams accommodationSearchParams, Pageable pageable) {
        log.info("Searching accommodations with params: {}", accommodationSearchParams);

        var accommodations = accommodationRepository.findWithFilters(accommodationSearchParams, pageable);

        return accommodations.map(mapper::toDto);
    }

    @Override
    @Transactional
    public void updateAccommodation(Long id, AccommodationUpdateRequest request) {
        log.info("Updating accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
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

        accommodationRepository.save(accommodation);
        log.info("Successfully updated accommodation with ID: {}", id);
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

        return accommodations.map(mapper::toDto);
    }
}
