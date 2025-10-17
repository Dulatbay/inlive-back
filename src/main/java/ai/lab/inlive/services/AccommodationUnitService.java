package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;

public interface AccommodationUnitService {
    void createUnit(AccommodationUnitCreateRequest request);
    AccUnitTariffResponse addTariff(Long unitId, AccUnitTariffCreateRequest request);
}
