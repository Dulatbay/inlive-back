package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.PriceRequestCreateRequest;
import ai.lab.inlive.dto.request.PriceRequestUpdateRequest;
import ai.lab.inlive.dto.request.PriceRequestClientResponseRequest;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PriceRequestService {
    /**
     * Создать новую заявку на цену
     */
    PriceRequestResponse createPriceRequest(PriceRequestCreateRequest request);

    /**
     * Обновить заявку на цену (для SUPER_MANAGER)
     * Может изменить статус: ACCEPTED, RAISED, DECREASED
     */
    PriceRequestResponse updatePriceRequest(Long priceRequestId, PriceRequestUpdateRequest request);

    /**
     * Скрыть заявку на цену (мягкое удаление)
     */
    void hidePriceRequest(Long priceRequestId);

    /**
     * Получить заявку на цену по ID
     */
    PriceRequestResponse getPriceRequestById(Long id);

    /**
     * Получить все активные заявки на цену для accommodation-unit
     */
    Page<PriceRequestResponse> getPriceRequestsByUnitId(Long unitId, Pageable pageable);

    /**
     * Получить все активные заявки на цену для search request
     */
    Page<PriceRequestResponse> getPriceRequestsBySearchRequestId(Long searchRequestId, Pageable pageable);

    /**
     * Клиент отвечает на заявку цены (ACCEPT или REJECT)
     * После получения предложения от объекта, клиент может принять (ACCEPTED) или отказать (REJECTED)
     */
    PriceRequestResponse respondToPriceRequest(Long priceRequestId, PriceRequestClientResponseRequest request, Long clientId);
}
