package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.request.AccSearchRequestCreateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndClients;
import ai.lab.inlive.services.AccSearchRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/search-requests")
@Tag(name = "Search Request", description = "API для работы с заявками на поиск жилья (для CLIENT)")
public class AccSearchRequestController {
    private final AccSearchRequestService accSearchRequestService;

    @AccessForAdminsAndClients
    @Operation(summary = "Создать заявку на поиск жилья (для CLIENT)",
               description = "Создание запроса на аренду квартиры или комнаты отеля. " +
                             "Система проверит наличие подходящих вариантов. " +
                             "ЕСЛИ к запросу есть соответствующие отели/квартиры, то создаем успешно запрос, " +
                             "иначе просим пользователя пересмотреть запрошенные параметры.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccSearchRequestResponse> createSearchRequest(
            @RequestBody @Valid AccSearchRequestCreateRequest request,
            Authentication authentication) {
        // Получаем ID текущего пользователя из Authentication
        Long authorId = Long.parseLong(authentication.getName());
        AccSearchRequestResponse response = accSearchRequestService.createSearchRequest(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Получить заявку на поиск по ID",
               description = "Получение детальной информации о заявке на поиск жилья")
    @GetMapping("/{id}")
    public ResponseEntity<AccSearchRequestResponse> getSearchRequestById(
            @Parameter(description = "ID заявки на поиск", example = "1")
            @PathVariable Long id) {
        AccSearchRequestResponse response = accSearchRequestService.getSearchRequestById(id);
        return ResponseEntity.ok(response);
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Получить мои заявки на поиск жилья",
               description = "Получение всех заявок текущего пользователя")
    @GetMapping("/my")
    public ResponseEntity<PaginatedResponse<AccSearchRequestResponse>> getMySearchRequests(
            Authentication authentication,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size) {
        Long authorId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("created_at")));
        Page<AccSearchRequestResponse> response = accSearchRequestService.getMySearchRequests(authorId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Обновить цену в заявке на поиск жилья (для CLIENT)",
               description = "После создания заявки можно изменить только цену. " +
                             "Другие параметры (район, услуги, условия, даты, количество людей) изменить нельзя. " +
                             "Если заявка не действительна, её нужно отменить.")
    @PatchMapping(value = "/{id}/price", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccSearchRequestResponse> updateSearchRequestPrice(
            @Parameter(description = "ID заявки на поиск", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid ai.lab.inlive.dto.request.AccSearchRequestUpdatePriceRequest request,
            Authentication authentication) {
        Long authorId = Long.parseLong(authentication.getName());
        AccSearchRequestResponse response = accSearchRequestService.updateSearchRequestPrice(id, request, authorId);
        return ResponseEntity.ok(response);
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Отменить заявку на поиск жилья (для CLIENT)",
               description = "Если заявка не действительна (ошибочные данные, изменились планы и т.д.), " +
                             "её нужно отменить, так как изменение основных параметров заявки не допускается")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AccSearchRequestResponse> cancelSearchRequest(
            @Parameter(description = "ID заявки на поиск", example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        Long authorId = Long.parseLong(authentication.getName());
        AccSearchRequestResponse response = accSearchRequestService.cancelSearchRequest(id, authorId);
        return ResponseEntity.ok(response);
    }
}
