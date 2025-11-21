package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.params.DictionarySearchParams;
import ai.lab.inlive.dto.request.DictionaryCreateRequest;
import ai.lab.inlive.dto.request.DictionaryUpdateRequest;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.security.authorization.AccessForAdmins;
import ai.lab.inlive.services.DictionaryService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dictionaries")
@Tag(name = "Dictionary", description = "API для работы со справочниками (услуги)")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @AccessForAdmins
    @Operation(summary = "Создать элемент справочника", description = "Создание нового элемента справочника")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Элемент справочника успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createDictionary(
            @RequestBody @Valid DictionaryCreateRequest request) {
        dictionaryService.createDictionary(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Получить элемент справочника по ID", description = "Получение элемента справочника по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент справочника успешно получен",
                    content = @Content(schema = @Schema(implementation = DictionaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Элемент справочника не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DictionaryResponse> getDictionaryById(
            @Parameter(description = "ID элемента справочника", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(dictionaryService.getDictionaryById(id));
    }

    @Operation(summary = "Получить все элементы справочника, соответствующие фильтрам", description = "Получение списка всех элементов справочника с возможностью фильтрации по параметрам")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список элементов справочника успешно получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<DictionaryResponse>> searchDictionaries(
            @ModelAttribute DictionarySearchParams dictionarySearchParams,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<DictionaryResponse> response = dictionaryService.searchWithParams(dictionarySearchParams, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Обновить элемент справочника", description = "Обновление данных элемента справочника")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент справочника успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Элемент справочника не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDictionary(
            @Parameter(description = "ID элемента справочника", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid DictionaryUpdateRequest request) {
        dictionaryService.updateDictionary(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить элемент справочника", description = "Мягкое удаление элемента справочника")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Элемент справочника успешно удален"),
            @ApiResponse(responseCode = "404", description = "Элемент справочника не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDictionary(
            @Parameter(description = "ID элемента справочника", example = "1")
            @PathVariable Long id) {
        dictionaryService.deleteDictionary(id);
        return ResponseEntity.noContent().build();
    }
}
