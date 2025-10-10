package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.CityCreateRequest;
import ai.lab.inlive.dto.request.CityFilterRequest;
import ai.lab.inlive.dto.request.CityUpdateRequest;
import ai.lab.inlive.dto.response.CityListResponse;
import ai.lab.inlive.dto.response.CityResponse;
import ai.lab.inlive.entities.City;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.repositories.CityRepository;
import ai.lab.inlive.services.CityService;
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
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;

    @Override
    public List<CityResponse> getAllCities() {
        log.info("Fetching all cities");
        List<City> cities = cityRepository.findAllByIsDeletedFalse();
        return cities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CityResponse mapToResponse(City city) {
        List<Long> districtIds = city.getDistricts().stream()
                .map(district -> district.getId())
                .collect(Collectors.toList());

        return new CityResponse(
                city.getId(),
                city.getName(),
                districtIds,
                city.getCreatedAt(),
                city.getUpdatedAt()
        );
    }
}
