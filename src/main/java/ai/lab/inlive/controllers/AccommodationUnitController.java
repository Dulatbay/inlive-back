package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.params.AccommodationUnitSearchParams;
import ai.lab.inlive.dto.request.AccUnitDictionariesUpdateRequest;
import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitUpdateRequest;
import ai.lab.inlive.dto.response.AccSearchRequestResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.dto.response.ReservationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.AccommodationUnitService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodation-units")
@Tag(name = "Accommodation Unit", description = "API для работы с единицами размещения")
public class AccommodationUnitController {
    private final AccommodationUnitService accommodationUnitService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать единицу размещения", description = "Создание новой квартиры/номера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Единица размещения успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN или SUPER_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Размещение или справочник не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createUnit(@ModelAttribute @Valid AccommodationUnitCreateRequest request) {
        accommodationUnitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Добавить тариф к единице размещения", description = "Прикрепление тарифа к квартире/номеру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Тариф успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или неверный тип справочника"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Единица размещения или справочник не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(value = "/{unitId}/tariffs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addTariff(
            @PathVariable Long unitId,
            @RequestBody @Valid AccUnitTariffCreateRequest request) {
        accommodationUnitService.addTariff(unitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Получить единицу размещения по ID", description = "Получение данных квартиры/номера по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Единица размещения успешно получена",
                    content = @Content(schema = @Schema(implementation = AccommodationUnitResponse.class))),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationUnitResponse> getUnitById(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(accommodationUnitService.getUnitById(id));
    }

    @Operation(summary = "Поиск единиц размещения по фильтрам", description = "Получение списка единиц размещения с возможностью фильтрации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список единиц размещения успешно получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Единица размещения успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUnit(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccommodationUnitUpdateRequest request) {
        accommodationUnitService.updateUnit(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить единицу размещения", description = "Мягкое удаление квартиры/номера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Единица размещения успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Услуги и условия успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или неверный тип справочника"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Единица размещения или справочник не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping(value = "/{unitId}/dictionaries", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateDictionaries(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long unitId,
            @RequestBody @Valid AccUnitDictionariesUpdateRequest request) {
        accommodationUnitService.updateDictionaries(unitId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить фото единицы размещения",
               description = "Полная замена фотографий единицы размещения. Все старые фото удаляются из S3 и базы данных, новые загружаются.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотографии успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные файлы или превышен лимит размера"),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера или ошибка загрузки в S3")
    })
    @PutMapping(path = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAccommodationUnitPhotos(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images) {
        accommodationUnitService.updateAccommodationUnitPhotos(id, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить фотографии единицы размещения",
            description = "Удаление выбранных фотографий по списку их URL или имен файлов. Фото удаляются из S3 и базы данных.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотографии успешно удалены"),
            @ApiResponse(responseCode = "400", description = "Некорректный список URL или пустой список"),
            @ApiResponse(responseCode = "404", description = "Единица размещения или фотографии не найдены"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера или ошибка удаления из S3")
    })
    @DeleteMapping(path = "/{id}/photos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAccommodationUnitPhotos(
            @Parameter(description = "ID единицы размещения", example = "1")
            @PathVariable Long id,
            @RequestBody List<String> photoUrls) {
        accommodationUnitService.deleteAccommodationUnitPhotos(id, photoUrls);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить релевантные заявки для единицы размещения",
            description = "Получение списка активных заявок, которые соответствуют данной квартире/номеру по всем критериям: " +
                    "услуги, условия, район, рейтинг, тип недвижимости. Показываются только заявки со статусами: " +
                    "OPEN_TO_PRICE_REQUEST (открыт к запросам по цене), PRICE_REQUEST_PENDING (был сделан запрос цены, но клиент не отреагировал), " +
                    "WAIT_TO_RESERVATION (запрос подтвержден, но отель не подтвердил бронь)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список релевантных заявок успешно получен"),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{unitId}/relevant-requests")
    public ResponseEntity<PaginatedResponse<AccSearchRequestResponse>> getRelevantRequests(
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
        Page<AccSearchRequestResponse> response = accommodationUnitService.getRelevantRequests(unitId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Получить релевантные единицы размещения для заявки",
            description = "Получение списка единиц размещения (units) из конкретного размещения (accommodation), " +
                    "которые соответствуют критериям конкретной заявки на поиск: тип недвижимости, рейтинг, район, услуги, условия")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список релевантных единиц размещения успешно получен"),
            @ApiResponse(responseCode = "404", description = "Размещение или заявка не найдены", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{accId}/by-request/{requestId}")
    public ResponseEntity<List<AccommodationUnitResponse>> getUnitsByAccommodationAndRequest(
            @Parameter(description = "ID размещения (accommodation)", example = "1")
            @PathVariable Long accId,
            @Parameter(description = "ID заявки на поиск жилья", example = "1")
            @PathVariable Long requestId) {
        List<AccommodationUnitResponse> response = accommodationUnitService.getUnitsByAccommodationAndRequest(accId, requestId);
        return ResponseEntity.ok(response);
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить заявки на цену для единицы размещения",
            description = "Получение всех активных заявок на цену для данной квартиры/номера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок на цену успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{unitId}/price-requests")
    public ResponseEntity<PaginatedResponse<PriceRequestResponse>> getUnitPriceRequests(
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
        Page<PriceRequestResponse> response = accommodationUnitService.getUnitPriceRequests(unitId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Получить ожидающие подтверждения бронирования для единицы размещения",
            description = "Получение всех бронирований со статусом WAITING_TO_APPROVE (требуют действия SUPER_MANAGER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ожидающих бронирований успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Единица размещения не найдена", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{unitId}/pending-reservations")
    public ResponseEntity<PaginatedResponse<ReservationResponse>> getUnitPendingReservations(
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
        Page<ReservationResponse> response = accommodationUnitService.getUnitPendingReservations(unitId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }
}
