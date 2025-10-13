package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.entities.District;
import ai.lab.inlive.repositories.DistrictRepository;
import ai.lab.inlive.services.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {
    private final DistrictRepository districtRepository;

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
