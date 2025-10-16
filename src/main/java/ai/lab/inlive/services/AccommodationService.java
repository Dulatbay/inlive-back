package ai.lab.inlive.services;

import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccommodationService {

    void createAccommodation(AccommodationCreateRequest request);

    AccommodationResponse getAccommodationById(Long id);

    Page<AccommodationResponse> searchWithParams(AccommodationSearchParams accommodationSearchParams, Pageable pageable);

    @Transactional
    void updateAccommodation(Long id, AccommodationUpdateRequest request);

    @Transactional
    void deleteAccommodation(Long id);

    @Transactional
    AccommodationResponse approveAccommodation(Long id, String approvedBy);

    @Transactional
    AccommodationResponse rejectAccommodation(Long id);

    List<AccommodationResponse> getAccommodationsByOwner(Long ownerId);

    List<AccommodationResponse> getPendingAccommodations();

    List<AccommodationResponse> getApprovedAccommodations();
}
