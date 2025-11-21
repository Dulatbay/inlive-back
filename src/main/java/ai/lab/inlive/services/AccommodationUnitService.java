package ai.lab.inlive.services;

import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.dto.request.AccUnitDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccommodationUnitService {
    void createUnit(AccommodationUnitCreateRequest request);

    void addTariff(Long unitId, AccUnitTariffCreateRequest request);

    AccommodationUnitResponse getUnitById(Long id);

    Page<AccommodationUnitResponse> searchWithParams(AccommodationUnitSearchParams params, Pageable pageable);

    void deleteUnit(Long id);

    void updateUnit(Long id, AccommodationUnitUpdateRequest request);

    void updateDictionaries(Long unitId, AccUnitDictionariesUpdateRequest request);

    void updateAccommodationUnitPhotos(Long id, List<MultipartFile> images);

    void deleteAccommodationUnitPhotos(Long id, List<String> photoUrls);

    Page<AccSearchRequestResponse> getRelevantRequests(Long unitId, Pageable pageable);

    Page<PriceRequestResponse> getUnitPriceRequests(Long unitId, Pageable pageable);

    Page<ReservationResponse> getUnitPendingReservations(Long unitId, Pageable pageable);

    List<AccommodationUnitResponse> getUnitsByAccommodationAndRequest(Long accommodationId, Long requestId);
}
