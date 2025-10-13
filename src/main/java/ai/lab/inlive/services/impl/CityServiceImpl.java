package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.response.CityResponse;
import ai.lab.inlive.entities.AbstractEntity;
import ai.lab.inlive.entities.City;
import ai.lab.inlive.repositories.CityRepository;
import ai.lab.inlive.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .map(AbstractEntity::getId)
                .collect(Collectors.toList());

        CityResponse response = new CityResponse();
        response.setId(city.getId());
        response.setName(city.getName());
        response.setDistrictIds(districtIds);
        response.setCreatedAt(city.getCreatedAt());
        response.setUpdatedAt(city.getUpdatedAt());
        return response;
    }
}
