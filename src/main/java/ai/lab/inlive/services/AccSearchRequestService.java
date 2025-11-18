package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.AccSearchRequestCreateRequest;
import ai.lab.inlive.dto.request.AccSearchRequestUpdatePriceRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccSearchRequestService {
    AccSearchRequestResponse createSearchRequest(AccSearchRequestCreateRequest request, String authorId);

    AccSearchRequestResponse getSearchRequestById(Long id);

    Page<AccSearchRequestResponse> getMySearchRequests(String authorId, Pageable pageable);

    AccSearchRequestResponse updateSearchRequestPrice(Long id, AccSearchRequestUpdatePriceRequest request, String authorId);

    AccSearchRequestResponse cancelSearchRequest(Long id, String authorId);
}
