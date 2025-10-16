package ai.lab.inlive.services;

import ai.lab.inlive.dto.response.CityResponse;

import java.util.List;

public interface CityService {
    List<CityResponse> getAllCities();
}
