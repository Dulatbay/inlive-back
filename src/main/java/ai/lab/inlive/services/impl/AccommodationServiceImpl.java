package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationFilterRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationListResponse;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.entities.Accommodation;
import ai.lab.inlive.entities.City;
import ai.lab.inlive.entities.District;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.repositories.AccommodationRepository;
import ai.lab.inlive.services.AccommodationService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public AccommodationResponse createAccommodation(AccommodationCreateRequest request) {
        log.info("Creating accommodation with name: {}", request.getName());

        City city = entityManager.getReference(City.class, request.getCityId());
        District district = entityManager.getReference(District.class, request.getDistrictId());

        Accommodation accommodation = new Accommodation();
        accommodation.setCity(city);
        accommodation.setDistrict(district);
        accommodation.setAddress(request.getAddress());
        accommodation.setName(request.getName());
        accommodation.setDescription(request.getDescription());
        accommodation.setOwnerId(request.getOwnerId());
        accommodation.setApproved(false);
        accommodation.setRating(0.0);

        Accommodation saved = accommodationRepository.save(accommodation);
        log.info("Successfully created accommodation with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public AccommodationResponse getAccommodationById(Long id) {
        log.info("Fetching accommodation by ID: {}", id);
        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));
        return mapToResponse(accommodation);
    }

    @Override
    public List<AccommodationResponse> getAllAccommodations() {
        log.info("Fetching all accommodations");
        List<Accommodation> accommodations = accommodationRepository.findAllByIsDeletedFalse();
        return accommodations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccommodationListResponse getAccommodationsWithFilters(AccommodationFilterRequest filterRequest) {
        log.info("Fetching accommodations with filters: {}", filterRequest);

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<Accommodation> accommodationsPage = accommodationRepository.findWithFilters(
                filterRequest.getCityId(),
                filterRequest.getDistrictId(),
                filterRequest.getApproved(),
                filterRequest.getOwnerId(),
                filterRequest.getMinRating(),
                filterRequest.getIsDeleted(),
                filterRequest.getName(),
                pageable
        );

        List<AccommodationResponse> accommodations = accommodationsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        AccommodationListResponse response = new AccommodationListResponse();
        response.setAccommodations(accommodations);
        response.setTotalPages(accommodationsPage.getTotalPages());
        response.setTotalElements(accommodationsPage.getTotalElements());
        response.setCurrentPage(accommodationsPage.getNumber());
        response.setPageSize(accommodationsPage.getSize());
        return response;
    }

    @Override
    @Transactional
    public AccommodationResponse updateAccommodation(Long id, AccommodationUpdateRequest request) {
        log.info("Updating accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        if (request.getCityId() != null) {
            City city = entityManager.getReference(City.class, request.getCityId());
            accommodation.setCity(city);
        }

        if (request.getDistrictId() != null) {
            District district = entityManager.getReference(District.class, request.getDistrictId());
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

        Accommodation updated = accommodationRepository.save(accommodation);
        log.info("Successfully updated accommodation with ID: {}", id);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        log.info("Deleting accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        // Use reflection to set the isDeleted field since there's no setter
        try {
            java.lang.reflect.Field isDeletedField = accommodation.getClass().getSuperclass().getDeclaredField("isDeleted");
            isDeletedField.setAccessible(true);
            isDeletedField.set(accommodation, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error setting isDeleted field", e);
            throw new RuntimeException("Error deleting accommodation", e);
        }

        accommodationRepository.save(accommodation);

        log.info("Successfully deleted accommodation with ID: {}", id);
    }

    @Override
    @Transactional
    public AccommodationResponse approveAccommodation(Long id, String approvedBy) {
        log.info("Approving accommodation with ID: {} by: {}", id, approvedBy);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        accommodation.setApproved(true);
        accommodation.setApprovedBy(approvedBy);

        Accommodation approved = accommodationRepository.save(accommodation);
        log.info("Successfully approved accommodation with ID: {}", id);

        return mapToResponse(approved);
    }

    @Override
    @Transactional
    public AccommodationResponse rejectAccommodation(Long id) {
        log.info("Rejecting accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));

        accommodation.setApproved(false);
        accommodation.setApprovedBy(null);

        Accommodation rejected = accommodationRepository.save(accommodation);
        log.info("Successfully rejected accommodation with ID: {}", id);

        return mapToResponse(rejected);
    }

    @Override
    public List<AccommodationResponse> getAccommodationsByOwner(String ownerId) {
        log.info("Fetching accommodations for owner: {}", ownerId);
        List<Accommodation> accommodations = accommodationRepository.findByOwnerIdAndIsDeletedFalse(ownerId);
        return accommodations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccommodationResponse> getPendingAccommodations() {
        log.info("Fetching pending accommodations");
        List<Accommodation> accommodations = accommodationRepository.findByApprovedAndIsDeletedFalse(false);
        return accommodations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccommodationResponse> getApprovedAccommodations() {
        log.info("Fetching approved accommodations");
        List<Accommodation> accommodations = accommodationRepository.findByApprovedAndIsDeletedFalse(true);
        return accommodations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AccommodationResponse mapToResponse(Accommodation accommodation) {
        AccommodationResponse response = new AccommodationResponse();
        response.setId(accommodation.getId());
        response.setCityId(accommodation.getCity().getId());
        response.setCityName(accommodation.getCity().getName());
        response.setDistrictId(accommodation.getDistrict().getId());
        response.setDistrictName(accommodation.getDistrict().getName());
        response.setAddress(accommodation.getAddress());
        response.setName(accommodation.getName());
        response.setDescription(accommodation.getDescription());
        response.setRating(accommodation.getRating());
        response.setApproved(accommodation.getApproved());
        response.setApprovedBy(accommodation.getApprovedBy());
        response.setOwnerId(accommodation.getOwnerId());
        response.setCreatedAt(accommodation.getCreatedAt());
        response.setUpdatedAt(accommodation.getUpdatedAt());
        return response;
    }
}
