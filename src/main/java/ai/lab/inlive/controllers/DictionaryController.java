package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.DictionaryCreateRequest;
import ai.lab.inlive.dto.request.DictionaryFilterRequest;
import ai.lab.inlive.dto.request.DictionaryUpdateRequest;
import ai.lab.inlive.dto.response.DictionaryListResponse;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.security.authorization.AccessForAdmins;
import ai.lab.inlive.services.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dictionaries")
@Tag(name = "Dictionary", description = "API для работы со справочниками (услуги)")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @AccessForAdmins
    @Operation(summary = "Создать элемент справочника", description = "Создание нового элемента справочника")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryResponse> createDictionary(
            @RequestBody @Valid DictionaryCreateRequest request) {
        log.info("Creating dictionary: {}", request.getKey());
        DictionaryResponse response = dictionaryService.createDictionary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить элемент справочника по ID", description = "Получение элемента справочника по идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<DictionaryResponse> getDictionaryById(
            @Parameter(description = "ID элемента справочника", example = "1")
            @PathVariable Long id) {
        log.info("Fetching dictionary by ID: {}", id);
        DictionaryResponse response = dictionaryService.getDictionaryById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить все элементы справочника", description = "Получение списка всех элементов справочника")
    @GetMapping
    public ResponseEntity<List<DictionaryResponse>> getAllDictionaries() {
        log.info("Fetching all dictionaries");
        List<DictionaryResponse> response = dictionaryService.getAllDictionaries();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Поиск элементов справочника с фильтрами", description = "Поиск элементов справочника с применением фильтров и пагинацией")
    @GetMapping("/search")
    public ResponseEntity<DictionaryListResponse> searchDictionaries(
            @Parameter(description = "Тип справочника") @RequestParam(required = false) String type,
            @Parameter(description = "Статус удаления (true - удаленные, false - активные)") @RequestParam(required = false) Boolean isDeleted,
            @Parameter(description = "Ключ (поиск по части ключа)") @RequestParam(required = false) String key,
            @Parameter(description = "Значение (поиск по части значения)") @RequestParam(required = false) String value,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        DictionaryFilterRequest filterRequest = new DictionaryFilterRequest(
                type, isDeleted, key, value, page, size, sortBy, sortDirection);

        log.info("Searching dictionaries with filters: {}", filterRequest);
        DictionaryListResponse response = dictionaryService.getDictionariesWithFilters(filterRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновить элемент справочника", description = "Обновление данных элемента справочника")
    @PutMapping("/{id}")
    public ResponseEntity<DictionaryResponse> updateDictionary(
            @Parameter(description = "ID элемента справочника", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid DictionaryUpdateRequest request) {
        log.info("Updating dictionary with ID: {}", id);
        DictionaryResponse response = dictionaryService.updateDictionary(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить элемент справочника", description = "Мягкое удаление элемента справочника")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDictionary(
            @Parameter(description = "ID элемента справочника", example = "1")
            @PathVariable Long id) {
        log.info("Deleting dictionary with ID: {}", id);
        dictionaryService.deleteDictionary(id);
        return ResponseEntity.noContent().build();
    }
}
