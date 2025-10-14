package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper mapper;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createAccommodation(AccommodationCreateRequest request) {
        log.info("Creating accommodation with name: {}", request.getName());

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND", "City not found with ID: " + request.getCityId()));
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DISTRICT_NOT_FOUND", "District not found with ID: " + request.getDistrictId()));
        User owner = userRepository.findById(Long.valueOf(request.getOwnerId()))
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with ID: " + request.getOwnerId()));

        Accommodation accommodation = new Accommodation();
        accommodation.setCity(city);
        accommodation.setDistrict(district);
        accommodation.setAddress(request.getAddress());
        accommodation.setName(request.getName());
        accommodation.setDescription(request.getDescription());
        accommodation.setOwnerId(owner);
        accommodation.setApproved(false);
        accommodation.setRating(0.0);

        accommodationRepository.save(accommodation);
        log.info("Successfully created accommodation with ID: {}", accommodation.getId());
    }

    @Override
    public AccommodationResponse getAccommodationById(Long id) {
        log.info("Fetching accommodation by ID: {}", id);
        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + id));
        return mapper.toDto(accommodation);
    }

    @Override
    public Page<AccommodationResponse> getAllAccommodations(Pageable pageable) {
        log.info("Fetching all accommodations");
        var accommodations = accommodationRepository.findAllByIsDeletedFalse(pageable);

        return accommodations.map(mapper::toDto);
    }

    @Override
    public Page<AccommodationResponse> searchWithParams(AccommodationSearchParams accommodationSearchParams, Pageable pageable) {
        log.info("Searching accommodations with params: {}", accommodationSearchParams);

        var accommodations = accommodationRepository.findWithFilters(
                accommodationSearchParams.getCityId(),
                accommodationSearchParams.getDistrictId(),
                accommodationSearchParams.getApproved(),
                accommodationSearchParams.getOwnerId(),
                accommodationSearchParams.getMinRating(),
                accommodationSearchParams.getIsDeleted(),
                accommodationSearchParams.getName(),
                pageable
        );

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

        User approver = userRepository.findById(Long.valueOf(approvedBy))
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with ID: " + approvedBy));
        accommodation.setApproved(true);
        accommodation.setApprovedBy(approver);

        Accommodation approved = accommodationRepository.save(accommodation);
        log.info("Successfully approved accommodation with ID: {}", id);

        return mapper.toDto(approved);
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

        return mapper.toDto(rejected);
    }

    @Override
    public List<AccommodationResponse> getAccommodationsByOwner(String ownerId) {
        log.info("Fetching accommodations for owner: {}", ownerId);
        List<Accommodation> accommodations = accommodationRepository.findByOwnerIdIdAndIsDeletedFalse(Long.valueOf(ownerId));
        return accommodations.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccommodationResponse> getPendingAccommodations() {
        log.info("Fetching pending accommodations");
        List<Accommodation> accommodations = accommodationRepository.findByApprovedAndIsDeletedFalse(false);
        return accommodations.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccommodationResponse> getApprovedAccommodations() {
        log.info("Fetching approved accommodations");
        List<Accommodation> accommodations = accommodationRepository.findByApprovedAndIsDeletedFalse(true);
        return accommodations.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
