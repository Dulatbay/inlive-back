package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.PriceRequestCreateRequest;
import ai.lab.inlive.dto.request.PriceRequestUpdateRequest;
import ai.lab.inlive.dto.request.PriceRequestClientResponseRequest;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PriceRequestService {
    void createPriceRequest(PriceRequestCreateRequest request);

    void updatePriceRequest(Long priceRequestId, PriceRequestUpdateRequest request);

    void hidePriceRequest(Long priceRequestId);

    PriceRequestResponse getPriceRequestById(Long id);

    Page<PriceRequestResponse> getPriceRequestsByUnitId(Long unitId, Pageable pageable);

    Page<PriceRequestResponse> getPriceRequestsBySearchRequestId(Long searchRequestId, Pageable pageable);

    void respondToPriceRequest(Long priceRequestId, PriceRequestClientResponseRequest request, String clientId);
}
