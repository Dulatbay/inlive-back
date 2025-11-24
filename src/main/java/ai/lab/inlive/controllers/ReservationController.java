package ai.lab.inlive.controllers;

import ai.lab.inlive.constants.Utils;
import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.request.ReservationCreateRequest;
import ai.lab.inlive.dto.request.ReservationFinalStatusUpdateRequest;
import ai.lab.inlive.dto.request.ReservationUpdateRequest;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndClients;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/reservations")
@Tag(name = "Reservation", description = "API для работы с бронированиями")
public class ReservationController {
    private final ReservationService reservationService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать бронирование",
            description = "Создание бронирования после подтверждения клиентом заявки на цену. " +
                    "Бронирование создается со статусом WAITING_TO_APPROVE и требует подтверждения SUPER_MANAGER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Бронирование успешно создано"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Заявка на цену не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createReservation(
            @RequestBody @Valid ReservationCreateRequest request) {
        reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить статус бронирования (для SUPER_MANAGER)",
            description = "SUPER_MANAGER может принять бронь (APPROVED) или отказать (REJECTED). " +
                    "На стадии MVP предоплата для брони не предусматривается.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус бронирования успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или недопустимое действие"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateReservationStatus(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid ReservationUpdateRequest request) {
        reservationService.updateReservationStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить финальный статус бронирования после прихода/неприхода клиента (для SUPER_MANAGER)",
            description = "После подтверждения брони (статус APPROVED), отель/владелец квартиры ждет прихода клиента. " +
                    "SUPER_MANAGER вручную отмечает: FINISHED_SUCCESSFUL (клиент пришел и заселился) или " +
                    "CLIENT_DIDNT_CAME (клиент не пришел)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Финальный статус успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или недопустимое действие"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping(value = "/{id}/final-status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateFinalStatus(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid ReservationFinalStatusUpdateRequest request) {
        reservationService.updateFinalStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить бронирование по ID",
            description = "Получение детальной информации о бронировании")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бронирование успешно получено",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить все бронирования для единицы размещения",
            description = "Получение всех бронирований для конкретной квартиры/номера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список бронирований успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
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
    @Operation(summary = "Получить все бронирования для размещения",
            description = "Получение всех бронирований для конкретного размещения (accommodation). " +
                    "Включает бронирования всех единиц размещения (units) в данном accommodation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список бронирований успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/by-accommodation/{accId}")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getReservationsByAccommodationId(
            @Parameter(description = "ID размещения (accommodation)", example = "1")
            @PathVariable Long accId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<ReservationResponse> response = reservationService.getReservationsByAccommodationId(accId, pageable);
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
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
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
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<ReservationResponse> response = reservationService.getReservationsBySearchRequestId(searchRequestId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Получить мои бронирования (для CLIENT)",
            description = "Получение всех бронирований текущего клиента. " +
                    "Клиент может видеть свои недавние брони для отслеживания и управления")
    @GetMapping("/my")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getMyReservations(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var clientId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<ReservationResponse> response = reservationService.getMyReservations(clientId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @ai.lab.inlive.security.authorization.AccessForAdminsAndClients
    @Operation(summary = "Отменить бронирование (для CLIENT)",
            description = "Клиент может преждевременно отменить свою бронь минимум за 1 день до даты заезда. " +
                    "При отмене статус брони меняется на CANCELED. " +
                    "ТОЛЬКО КЛИЕНТ может отменить свою бронь. " +
                    "Можно отменить брони в статусах WAITING_TO_APPROVE или APPROVED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Бронирование успешно отменено"),
            @ApiResponse(responseCode = "400", description = "Отмена невозможна - слишком поздно (менее 1 дня до заезда) или недопустимый статус"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - можно отменять только свои бронирования"),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var clientId = Utils.extractIdFromToken(token);

        reservationService.cancelReservation(id, clientId);
        return ResponseEntity.noContent().build();
    }
}
