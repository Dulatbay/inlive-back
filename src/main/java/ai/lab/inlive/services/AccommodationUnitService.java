package ai.lab.inlive.services;

import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.dto.request.AccUnitDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccommodationUnitService {
    void createUnit(AccommodationUnitCreateRequest request);
    AccUnitTariffResponse addTariff(Long unitId, AccUnitTariffCreateRequest request);

    AccommodationUnitResponse getUnitById(Long id);

    Page<AccommodationUnitResponse> searchWithParams(AccommodationUnitSearchParams params, Pageable pageable);

    void deleteUnit(Long id);

    void updateUnit(Long id, AccommodationUnitUpdateRequest request);

    void updateDictionaries(Long unitId, AccUnitDictionariesUpdateRequest request);

    List<DictionaryResponse> getUnitServices(Long unitId);

    List<DictionaryResponse> getUnitConditions(Long unitId);

    Page<AccSearchRequestResponse> getRelevantRequests(Long unitId, Pageable pageable);

    Page<PriceRequestResponse> getUnitPriceRequests(Long unitId, Pageable pageable);

    Page<ReservationResponse> getUnitPendingReservations(Long unitId, Pageable pageable);
}
