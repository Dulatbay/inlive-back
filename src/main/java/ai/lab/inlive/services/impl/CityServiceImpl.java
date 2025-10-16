package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.response.CityResponse;
import ai.lab.inlive.entities.City;
import ai.lab.inlive.mappers.CityMapper;
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
    private final CityMapper mapper;

    @Override
    public List<CityResponse> getAllCities() {
        log.info("Fetching all cities");
        List<City> cities = cityRepository.findAllByIsDeletedFalse();
        return cities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
