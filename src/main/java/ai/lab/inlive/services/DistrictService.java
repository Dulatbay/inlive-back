package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.DistrictCreateRequest;
import ai.lab.inlive.dto.request.DistrictFilterRequest;
import ai.lab.inlive.dto.request.DistrictUpdateRequest;
import ai.lab.inlive.dto.response.DistrictListResponse;
import ai.lab.inlive.dto.response.DistrictResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface DistrictService {
    List<DistrictResponse> getAllDistricts();

    List<DistrictResponse> getDistrictsByCity(Long cityId);
}
