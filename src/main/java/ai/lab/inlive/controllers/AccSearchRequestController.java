package ai.lab.inlive.controllers;

import ai.lab.inlive.constants.Utils;
import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.request.AccSearchRequestCreateRequest;
import ai.lab.inlive.dto.request.AccSearchRequestUpdatePriceRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndClients;
import ai.lab.inlive.services.AccSearchRequestService;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заявка на поиск успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или нет подходящих вариантов"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Район или справочник не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createSearchRequest(@RequestBody @Valid AccSearchRequestCreateRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorId = Utils.extractIdFromToken(token);

        accSearchRequestService.createSearchRequest(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Получить заявку на поиск по ID",
            description = "Получение детальной информации о заявке на поиск жилья")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка успешно получена",
                    content = @Content(schema = @Schema(implementation = AccSearchRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccSearchRequestResponse> getSearchRequestById(
            @Parameter(description = "ID заявки на поиск", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(accSearchRequestService.getSearchRequestById(id));
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Получить мои заявки на поиск жилья",
            description = "Получение всех заявок текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<PaginatedResponse<AccSearchRequestResponse>> getMySearchRequests(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccSearchRequestResponse> response = accSearchRequestService.getMySearchRequests(authorId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Обновить цену в заявке на поиск жилья (для CLIENT)",
            description = "После создания заявки можно изменить только цену. " +
                    "Другие параметры (район, услуги, условия, даты, количество людей) изменить нельзя. " +
                    "Если заявка не действительна, её нужно отменить.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Цена успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или недопустимый статус заявки"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - можно обновлять только свои заявки"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping(value = "/{id}/price", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateSearchRequestPrice(
            @Parameter(description = "ID заявки на поиск", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccSearchRequestUpdatePriceRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorId = Utils.extractIdFromToken(token);

        accSearchRequestService.updateSearchRequestPrice(id, request, authorId);
        return ResponseEntity.ok().build();
    }

    @AccessForAdminsAndClients
    @Operation(summary = "Отменить заявку на поиск жилья (для CLIENT)",
            description = "Если заявка не действительна (ошибочные данные, изменились планы и т.д.), " +
                    "её нужно отменить, так как изменение основных параметров заявки не допускается")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Заявка успешно отменена"),
            @ApiResponse(responseCode = "400", description = "Недопустимый статус заявки для отмены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - можно отменять только свои заявки"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSearchRequest(
            @Parameter(description = "ID заявки на поиск", example = "1")
            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorId = Utils.extractIdFromToken(token);

        accSearchRequestService.cancelSearchRequest(id, authorId);
        return ResponseEntity.noContent().build();
    }
}
