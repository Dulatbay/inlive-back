package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.request.ReservationCreateRequest;
import ai.lab.inlive.dto.request.ReservationFinalStatusUpdateRequest;
import ai.lab.inlive.dto.request.ReservationUpdateRequest;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.ReservationService;
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
@RequestMapping("/reservations")
@Tag(name = "Reservation", description = "API для работы с бронированиями")
public class ReservationController {
    private final ReservationService reservationService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать бронирование",
               description = "Создание бронирования после подтверждения клиентом заявки на цену. " +
                             "Бронирование создается со статусом WAITING_TO_APPROVE и требует подтверждения SUPER_MANAGER")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid ReservationCreateRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить статус бронирования (для SUPER_MANAGER)",
               description = "SUPER_MANAGER может принять бронь (APPROVED) или отказать (REJECTED). " +
                             "На стадии MVP предоплата для брони не предусматривается.")
    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid ReservationUpdateRequest request) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить финальный статус бронирования после прихода/неприхода клиента (для SUPER_MANAGER)",
               description = "После подтверждения брони (статус APPROVED), отель/владелец квартиры ждет прихода клиента. " +
                             "SUPER_MANAGER вручную отмечает: FINISHED_SUCCESSFUL (клиент пришел и заселился) или " +
                             "CLIENT_DIDNT_CAME (клиент не пришел)")
    @PutMapping(value = "/{id}/final-status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> updateFinalStatus(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid ReservationFinalStatusUpdateRequest request) {
        ReservationResponse response = reservationService.updateFinalStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить бронирование по ID",
               description = "Получение детальной информации о бронировании")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id) {
        ReservationResponse response = reservationService.getReservationById(id);
        return ResponseEntity.ok(response);
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить все бронирования для единицы размещения",
               description = "Получение всех бронирований для конкретной квартиры/номера")
    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getReservationsByUnitId(
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
        Page<ReservationResponse> response = reservationService.getReservationsByUnitId(unitId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить ожидающие подтверждения бронирования для единицы размещения",
               description = "Получение всех бронирований со статусом WAITING_TO_APPROVE для конкретной квартиры/номера. " +
                             "Это бронирования, требующие действия SUPER_MANAGER (принять или отказать)")
    @GetMapping("/by-unit/{unitId}/pending")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getPendingReservationsByUnitId(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReservationResponse> response = reservationService.getPendingReservationsByUnitId(unitId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Получить бронирования для заявки на поиск жилья",
               description = "Получение всех бронирований, связанных с конкретной заявкой на поиск жилья")
    @GetMapping("/by-search-request/{searchRequestId}")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getReservationsBySearchRequestId(
            @Parameter(description = "ID заявки на поиск жилья", example = "1")
            @PathVariable Long searchRequestId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReservationResponse> response = reservationService.getReservationsBySearchRequestId(searchRequestId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @ai.lab.inlive.security.authorization.AccessForAdminsAndClients
    @Operation(summary = "Получить мои бронирования (для CLIENT)",
               description = "Получение всех бронирований текущего клиента. " +
                             "Клиент может видеть свои недавние брони для отслеживания и управления")
    @GetMapping("/my")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getMyReservations(
            org.springframework.security.core.Authentication authentication,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size) {
        Long clientId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReservationResponse> response = reservationService.getMyReservations(clientId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @ai.lab.inlive.security.authorization.AccessForAdminsAndClients
    @Operation(summary = "Отменить бронирование (для CLIENT)",
               description = "Клиент может преждевременно отменить свою бронь минимум за 1 день до даты заезда. " +
                             "При отмене статус брони меняется на CANCELED. " +
                             "ТОЛЬКО КЛИЕНТ может отменить свою бронь. " +
                             "Можно отменить брони в статусах WAITING_TO_APPROVE или APPROVED")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        Long clientId = Long.parseLong(authentication.getName());
        ReservationResponse response = reservationService.cancelReservation(id, clientId);
        return ResponseEntity.ok(response);
    }
}
