package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationFilterRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationListResponse;
import ai.lab.inlive.dto.response.AccommodationResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AccommodationService {

    AccommodationResponse createAccommodation(AccommodationCreateRequest request);

    AccommodationResponse getAccommodationById(Long id);

    List<AccommodationResponse> getAllAccommodations();

    AccommodationListResponse getAccommodationsWithFilters(AccommodationFilterRequest filterRequest);

    @Transactional
    AccommodationResponse updateAccommodation(Long id, AccommodationUpdateRequest request);

    @Transactional
    void deleteAccommodation(Long id);

    @Transactional
    AccommodationResponse approveAccommodation(Long id, String approvedBy);

    @Transactional
    AccommodationResponse rejectAccommodation(Long id);

    List<AccommodationResponse> getAccommodationsByOwner(String ownerId);

    List<AccommodationResponse> getPendingAccommodations();

    List<AccommodationResponse> getApprovedAccommodations();
}
