package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.AccommodationService;
import ai.lab.inlive.constants.Utils;
import ai.lab.inlive.validators.ValidFiles;
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
import org.springframework.validation.annotation.Validated;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodations")
@Tag(name = "Accommodation", description = "API для работы с размещениями")
public class AccommodationController {
    private final AccommodationService accommodationService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать размещение", description = "Создание нового размещения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Размещение успешно создано"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN или SUPER_MANAGER", content = @Content),
            @ApiResponse(responseCode = "404", description = "Город, район или владелец не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createAccommodation(
            @ModelAttribute @Valid AccommodationCreateRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var createdByUserId = Utils.extractIdFromToken(token);

        accommodationService.createAccommodation(request, createdByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Получить размещение по ID", description = "Получение размещения по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Размещение успешно получено",
                    content = @Content(schema = @Schema(implementation = AccommodationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Размещение не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationResponse> getAccommodationById(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(accommodationService.getAccommodationById(id));
    }

    @Operation(summary = "Получить все размещения, соответствующие фильтрам", description = "Получение списка размещений с возможностью фильтрации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список размещений успешно получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<AccommodationResponse>> searchAccommodations(
            @ModelAttribute AccommodationSearchParams accommodationSearchParams,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccommodationResponse> response = accommodationService.searchWithParams(accommodationSearchParams, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Обновить размещение", description = "Обновление данных размещения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Размещение успешно обновлено"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PutMapping("/{id}/main-info")
    public ResponseEntity<Void> updateAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccommodationUpdateRequest request) {
        accommodationService.updateAccommodation(id, request);
        return ResponseEntity.ok().build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Обновить услуги и условия размещения",
               description = "Обновление списков услуг (SERVICES) и условий (CONDITIONS) для размещения. Существующие списки будут заменены новыми.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Услуги и условия успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или неверный тип справочника", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN или SUPER_MANAGER", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение или справочник не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PutMapping(value = "/{id}/dictionaries", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateDictionaries(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccommodationDictionariesUpdateRequest request) {
        accommodationService.updateDictionaries(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить фото размещения",
               description = "Полная замена фотографий размещения. Все старые фото удаляются из S3 и базы данных, новые загружаются.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотографии успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные файлы или превышен лимит размера", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера или ошибка загрузки в S3", content = @Content)
    })
    @PutMapping(path = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAccommodationPhotos(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @ValidFiles @RequestPart("images") List<MultipartFile> images) {
        accommodationService.updateAccommodationPhotos(id, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить фотографии размещения",
               description = "Удаление выбранных фотографий по списку их URL или имен файлов. Фото удаляются из S3 и базы данных.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотографии успешно удалены"),
            @ApiResponse(responseCode = "400", description = "Некорректный список URL или пустой список", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение или фотографии не найдены", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера или ошибка удаления из S3", content = @Content)
    })
    @DeleteMapping(path = "/{id}/photos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAccommodationPhotos(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @RequestBody List<String> photoUrls) {
        accommodationService.deleteAccommodationPhotos(id, photoUrls);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить размещение", description = "Мягкое удаление размещения (устанавливает флаг isDeleted)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Размещение успешно удалено"),
            @ApiResponse(responseCode = "404", description = "Размещение не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Одобрить размещение", description = "Одобрение размещения администратором (устанавливает approved=true)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Размещение успешно одобрено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение или пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var approvedByUserId = Utils.extractIdFromToken(token);

        accommodationService.approveAccommodation(id, approvedByUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отклонить размещение", description = "Отклонение размещения администратором (устанавливает approved=false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Размещение успешно отклонено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Размещение или пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var approvedByUserId = Utils.extractIdFromToken(token);

        accommodationService.rejectAccommodation(id, approvedByUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Получить размещения владельца", description = "Получение всех размещений определенного владельца")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список размещений успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/owner/search")
    public ResponseEntity<PaginatedResponse<AccommodationResponse>> getAccommodationsByOwner(
            @ModelAttribute AccommodationSearchParams accommodationSearchParams,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var ownerId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccommodationResponse> response = accommodationService.getAccommodationsByOwner(ownerId, accommodationSearchParams, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Получить релевантные заявки для размещения",
            description = "Получение списка активных заявок, которые соответствуют данному размещению по всем критериям: " +
                    "услуги, условия, район, рейтинг, тип недвижимости. Ищет заявки, которые подходят хотя бы для одного unit в этом accommodation. " +
                    "Показываются только заявки со статусами: OPEN_TO_PRICE_REQUEST, PRICE_REQUEST_PENDING, WAIT_TO_RESERVATION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список релевантных заявок успешно получен"),
            @ApiResponse(responseCode = "404", description = "Размещение не найдено", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{id}/relevant-requests")
    public ResponseEntity<PaginatedResponse<AccSearchRequestResponse>> getRelevantRequests(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccSearchRequestResponse> response = accommodationService.getRelevantRequests(id, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }
}
