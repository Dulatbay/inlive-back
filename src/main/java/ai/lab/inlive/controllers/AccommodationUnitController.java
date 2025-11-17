package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.dto.request.AccUnitDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.AccommodationUnitService;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodation-units")
@Tag(name = "Accommodation Unit", description = "API для работы с единицами размещения")
public class AccommodationUnitController {
    private final AccommodationUnitService accommodationUnitService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать единицу размещения", description = "Создание новой квартиры/номера")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createUnit(@ModelAttribute @Valid AccommodationUnitCreateRequest request) {
        accommodationUnitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Добавить тариф к единице размещения", description = "Прикрепление тарифа к квартире/номеру")
    @PostMapping(value = "/{unitId}/tariffs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccUnitTariffResponse> addTariff(
            @PathVariable Long unitId,
            @RequestBody @Valid AccUnitTariffCreateRequest request) {
        AccUnitTariffResponse response = accommodationUnitService.addTariff(unitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить единицу размещения по ID", description = "Получение данных квартиры/номера по идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationUnitResponse> getUnitById(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id) {
        AccommodationUnitResponse response = accommodationUnitService.getUnitById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Поиск единиц размещения по фильтрам", description = "Получение списка единиц размещения с возможностью фильтрации")
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<AccommodationUnitResponse>> searchUnits(
            @ModelAttribute AccommodationUnitSearchParams params,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccommodationUnitResponse> response = accommodationUnitService.searchWithParams(params, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Обновить единицу размещения", description = "Обновление данных квартиры/номера")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUnit(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccommodationUnitUpdateRequest request) {
        accommodationUnitService.updateUnit(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить единицу размещения", description = "Мягкое удаление квартиры/номера")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id) {
        accommodationUnitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить услуги и условия единицы размещения",
               description = "Обновление списков услуг (SERVICES) и условий (CONDITIONS) для квартиры/номера. Существующие списки будут заменены новыми.")
    @PutMapping(value = "/{unitId}/dictionaries", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateDictionaries(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @RequestBody @Valid AccUnitDictionariesUpdateRequest request) {
        accommodationUnitService.updateDictionaries(unitId, request);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Получить релевантные заявки для единицы размещения",
               description = "Получение списка активных заявок, которые соответствуют данной квартире/номеру по всем критериям: " +
                       "услуги, условия, район, рейтинг, тип недвижимости. Показываются только заявки со статусами: " +
                       "OPEN_TO_PRICE_REQUEST (открыт к запросам по цене), PRICE_REQUEST_PENDING (был сделан запрос цены, но клиент не отреагировал), " +
                       "WAIT_TO_RESERVATION (запрос подтвержден, но отель не подтвердил бронь)")
    @GetMapping("/{unitId}/relevant-requests")
    public ResponseEntity<PaginatedResponse<AccSearchRequestResponse>> getRelevantRequests(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "created_at") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccSearchRequestResponse> response = accommodationUnitService.getRelevantRequests(unitId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить заявки на цену для единицы размещения",
               description = "Получение всех активных заявок на цену для данной квартиры/номера")
    @GetMapping("/{unitId}/price-requests")
    public ResponseEntity<PaginatedResponse<PriceRequestResponse>> getUnitPriceRequests(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<PriceRequestResponse> response = accommodationUnitService.getUnitPriceRequests(unitId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить ожидающие подтверждения бронирования для единицы размещения",
               description = "Получение всех бронирований со статусом WAITING_TO_APPROVE (требуют действия SUPER_MANAGER)")
    @GetMapping("/{unitId}/pending-reservations")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getUnitPendingReservations(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReservationResponse> response = accommodationUnitService.getUnitPendingReservations(unitId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }
}
