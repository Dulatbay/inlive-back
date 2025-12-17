package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.entities.District;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.DistrictMapper;
import ai.lab.inlive.repositories.CityRepository;
import ai.lab.inlive.repositories.DistrictRepository;
import ai.lab.inlive.services.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final DistrictMapper mapper;
    private final MessageSource messageSource;

    @Override
    public List<DistrictResponse> getAllDistricts() {
        log.info("Fetching all districts");
        List<District> districts = districtRepository.findAllByIsDeletedFalse();
        return districts.stream()
                .map(this::mapToResponseWithAvgPrice)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictResponse> getDistrictsByCity(Long cityId) {
        log.info("Fetching districts for city ID: {}", cityId);
        
        cityRepository.findByIdAndIsDeletedFalse(cityId)
                .orElseThrow(() -> new DbObjectNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "CITY_NOT_FOUND",
                        messageSource.getMessage("services.district.cityNotFound", 
                                new Object[]{cityId}, LocaleContextHolder.getLocale())
                ));
        
        List<District> districts = districtRepository.findByCityIdAndIsDeletedFalse(cityId);
        return districts.stream()
                .map(this::mapToResponseWithAvgPrice)
                .collect(Collectors.toList());
    }

    private DistrictResponse mapToResponseWithAvgPrice(District district) {
        Double avgPrice = districtRepository.calculateAveragePriceByDistrictId(district.getId());
        return mapper.toDtoWithAvgPrice(district, avgPrice);
    }
}
