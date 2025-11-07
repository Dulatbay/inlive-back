package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.AccSearchRequestCreateRequest;
import ai.lab.inlive.dto.request.AccSearchRequestUpdatePriceRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccSearchRequestService {
    /**
     * Создать заявку на поиск жилья (для CLIENT)
     * Проверяет наличие подходящих вариантов перед созданием
     */
    AccSearchRequestResponse createSearchRequest(AccSearchRequestCreateRequest request, Long authorId);

    /**
     * Получить заявку по ID
     */
    AccSearchRequestResponse getSearchRequestById(Long id);

    /**
     * Получить все заявки пользователя
     */
    Page<AccSearchRequestResponse> getMySearchRequests(Long authorId, Pageable pageable);

    /**
     * Обновить только цену в заявке на поиск жилья
     * После создания заявки можно изменить только цену
     */
    AccSearchRequestResponse updateSearchRequestPrice(Long id, AccSearchRequestUpdatePriceRequest request, Long authorId);

    /**
     * Отменить заявку на поиск жилья
     * Если заявка не действительна, её нужно отменить
     */
    AccSearchRequestResponse cancelSearchRequest(Long id, Long authorId);
}
