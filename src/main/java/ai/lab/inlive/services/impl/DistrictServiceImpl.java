package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.DistrictCreateRequest;
import ai.lab.inlive.dto.request.DistrictFilterRequest;
import ai.lab.inlive.dto.request.DistrictUpdateRequest;
import ai.lab.inlive.dto.response.DistrictListResponse;
import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.entities.City;
import ai.lab.inlive.entities.District;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.repositories.DistrictRepository;
import ai.lab.inlive.services.DistrictService;
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
public class DistrictServiceImpl implements DistrictService {
    private final DistrictRepository districtRepository;
    private final EntityManager entityManager;

    @Override
    public List<DistrictResponse> getAllDistricts() {
        log.info("Fetching all districts");
        List<District> districts = districtRepository.findAllByIsDeletedFalse();
        return districts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictResponse> getDistrictsByCity(Long cityId) {
        log.info("Fetching districts for city ID: {}", cityId);
        List<District> districts = districtRepository.findByCityIdAndIsDeletedFalse(cityId);
        return districts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DistrictResponse createDistrict(DistrictCreateRequest request) {
        log.info("Creating district with name: {} for city ID: {}", request.getName(), request.getCityId());

        if (districtRepository.existsByNameAndCityIdAndIsDeletedFalse(request.getName(), request.getCityId())) {
            throw new RuntimeException("District with name '" + request.getName() + "' already exists in this city");
        }

        City city = entityManager.getReference(City.class, request.getCityId());

        District district = new District();
        district.setCity(city);
        district.setName(request.getName());

        District saved = districtRepository.save(district);
        log.info("Successfully created district with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public DistrictListResponse getDistrictsWithFilters(DistrictFilterRequest filterRequest) {
        log.info("Fetching districts with filters: {}", filterRequest);

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<District> districtsPage = districtRepository.findWithFilters(
                filterRequest.getCityId(),
                filterRequest.getIsDeleted(),
                filterRequest.getName(),
                pageable
        );

        List<DistrictResponse> districts = districtsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        DistrictListResponse response = new DistrictListResponse();
        response.setDistricts(districts);
        response.setTotalPages(districtsPage.getTotalPages());
        response.setTotalElements(districtsPage.getTotalElements());
        response.setCurrentPage(districtsPage.getNumber());
        response.setPageSize(districtsPage.getSize());
        return response;
    }

    @Override
    @Transactional
    public DistrictResponse updateDistrict(Long id, DistrictUpdateRequest request) {
        log.info("Updating district with ID: {}", id);

        District district = districtRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DISTRICT_NOT_FOUND", "District not found with ID: " + id));

        if (request.getCityId() != null) {
            City city = entityManager.getReference(City.class, request.getCityId());
            district.setCity(city);
        }

        if (request.getName() != null) {
            Long cityId = request.getCityId() != null ? request.getCityId() : district.getCity().getId();
            if (districtRepository.existsByNameAndCityIdAndIsDeletedFalse(request.getName(), cityId)
                    && !district.getName().equals(request.getName())) {
                throw new RuntimeException("District with name '" + request.getName() + "' already exists in this city");
            }
            district.setName(request.getName());
        }

        District updated = districtRepository.save(district);
        log.info("Successfully updated district with ID: {}", id);

        return mapToResponse(updated);
    }

    private DistrictResponse mapToResponse(District district) {
        DistrictResponse response = new DistrictResponse();
        response.setId(district.getId());
        response.setCityId(district.getCity().getId());
        response.setCityName(district.getCity().getName());
        response.setName(district.getName());
        response.setCreatedAt(district.getCreatedAt());
        response.setUpdatedAt(district.getUpdatedAt());
        return response;
    }
}
