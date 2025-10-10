package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.CityCreateRequest;
import ai.lab.inlive.dto.request.CityFilterRequest;
import ai.lab.inlive.dto.request.CityUpdateRequest;
import ai.lab.inlive.dto.response.CityListResponse;
import ai.lab.inlive.dto.response.CityResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CityService {
    List<CityResponse> getAllCities();
}
