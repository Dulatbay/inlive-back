package ai.lab.inlive.controllers;

import ai.lab.inlive.constants.Utils;
import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.request.PriceRequestCreateRequest;
import ai.lab.inlive.dto.request.PriceRequestUpdateRequest;
import ai.lab.inlive.dto.request.PriceRequestClientResponseRequest;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.security.authorization.AccessForAdminsAndClients;
import ai.lab.inlive.services.PriceRequestService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/price-requests")
@Tag(name = "Price Request", description = "API для работы с заявками на цену")
public class PriceRequestController {
    private final PriceRequestService priceRequestService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать заявку на цену",
            description = "Создание новой заявки на цену для конкретной единицы размещения и заявки на поиск жилья")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createPriceRequest(
            @RequestBody @Valid PriceRequestCreateRequest request) {
        priceRequestService.createPriceRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить заявку на цену (для SUPER_MANAGER)",
            description = "SUPER_MANAGER может: повысить цену (RAISED), принять текущую цену (ACCEPTED), понизить цену (DECREASED)")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePriceRequest(
            @Parameter(description = "ID заявки на цену", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid PriceRequestUpdateRequest request) {
        priceRequestService.updatePriceRequest(id, request);
        return ResponseEntity.ok().build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Скрыть заявку на цену",
            description = "SUPER_MANAGER может скрыть заявку с экрана (мягкое удаление)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> hidePriceRequest(
            @Parameter(description = "ID заявки на цену", example = "1")
            @PathVariable Long id) {
        priceRequestService.hidePriceRequest(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить заявку на цену по ID",
            description = "Получение детальной информации о заявке на цену")
    @GetMapping("/{id}")
    public ResponseEntity<PriceRequestResponse> getPriceRequestById(
            @Parameter(description = "ID заявки на цену", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(priceRequestService.getPriceRequestById(id));
    }

    @Operation(summary = "Получить заявки на цену для единицы размещения",
            description = "Получение всех активных заявок на цену для конкретной квартиры/номера")
    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<PaginatedResponse<PriceRequestResponse>> getPriceRequestsByUnitId(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<PriceRequestResponse> response = priceRequestService.getPriceRequestsByUnitId(unitId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Получить заявки на цену для заявки на поиск жилья",
            description = "Получение всех активных заявок на цену, связанных с конкретной заявкой на поиск жилья")
    @GetMapping("/by-search-request/{searchRequestId}")
    public ResponseEntity<PaginatedResponse<PriceRequestResponse>> getPriceRequestsBySearchRequestId(
            @Parameter(description = "ID заявки на поиск жилья", example = "1")
            @PathVariable Long searchRequestId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<PriceRequestResponse> response = priceRequestService.getPriceRequestsBySearchRequestId(searchRequestId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Ответить на заявку цены (для CLIENT)",
            description = "После получения предложения от объекта (с ценой ACCEPTED/RAISED/DECREASED), " +
                    "клиент может принять предложение (ACCEPTED) или отказать (REJECTED). " +
                    "При принятии заявка переходит в статус ожидания резервации.")
    @PatchMapping(value = "/{id}/respond", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> respondToPriceRequest(
            @Parameter(description = "ID заявки на цену", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid PriceRequestClientResponseRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var clientId = Utils.extractIdFromToken(token);

        priceRequestService.respondToPriceRequest(id, request, clientId);
        return ResponseEntity.noContent().build();
    }
}
