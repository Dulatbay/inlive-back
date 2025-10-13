package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// todo: доработать контроллер
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodations")
@Tag(name = "Accommodation", description = "API для работы с размещениями")
public class AccommodationController {
    private final AccommodationService accommodationService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать размещение", description = "Создание нового размещения")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createAccommodation(
            @RequestBody @Valid AccommodationCreateRequest request) {
        log.info("Creating accommodation: {}", request.getName());
        AccommodationResponse response = accommodationService.createAccommodation(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить размещение по ID", description = "Получение размещения по идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationResponse> getAccommodationById(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        log.info("Fetching accommodation by ID: {}", id);
        AccommodationResponse response = accommodationService.getAccommodationById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить все размещения", description = "Получение списка всех размещений")
    @GetMapping
    public ResponseEntity<List<AccommodationResponse>> getAllAccommodations() {
        log.info("Fetching all accommodations");
        List<AccommodationResponse> response = accommodationService.getAllAccommodations();
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "Поиск размещений с фильтрами", description = "Поиск размещений с применением фильтров и пагинацией")
//    @GetMapping("/search")
//    public ResponseEntity<PaginatedResponse<AccommodationResponse>> searchAccommodations(
//            @Parameter(description = "ID города") @RequestParam(required = false) Long cityId,
//            @Parameter(description = "ID района") @RequestParam(required = false) Long districtId,
//            @Parameter(description = "Статус одобрения") @RequestParam(required = false) Boolean approved,
//            @Parameter(description = "ID владельца") @RequestParam(required = false) String ownerId,
//            @Parameter(description = "Минимальный рейтинг") @RequestParam(required = false) Double minRating,
//            @Parameter(description = "Статус удаления (true - удаленные, false - активные)") @RequestParam(required = false) Boolean isDeleted,
//            @Parameter(description = "Название (поиск по части названия)") @RequestParam(required = false) String name,
//            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
//            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
//            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
//            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
//
//        AccommodationFilterRequest filterRequest = new AccommodationFilterRequest(
//                cityId, districtId, approved, ownerId, minRating, isDeleted, name,
//                page, size, sortBy, sortDirection);
//
//        log.info("Searching accommodations with filters: {}", filterRequest);
//        Page<AccommodationResponse> accommodationResponsePage = accommodationService.getAccommodationsWithFilters(filterRequest);
//        PaginatedResponse<AccommodationListResponse> response = new PaginatedResponse<>(accommodationResponsePage);
//        return ResponseEntity.ok(response);
//    }

    @Operation(summary = "Обновить размещение", description = "Обновление данных размещения")
    @PutMapping("/{id}")
    public ResponseEntity<AccommodationResponse> updateAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccommodationUpdateRequest request) {
        log.info("Updating accommodation with ID: {}", id);
        AccommodationResponse response = accommodationService.updateAccommodation(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить размещение", description = "Мягкое удаление размещения")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        log.info("Deleting accommodation with ID: {}", id);
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Одобрить размещение", description = "Одобрение размещения администратором")
    @PostMapping("/{id}/approve")
    public ResponseEntity<AccommodationResponse> approveAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID пользователя, который одобряет")
            @RequestParam String approvedBy) {
        log.info("Approving accommodation with ID: {} by: {}", id, approvedBy);
        AccommodationResponse response = accommodationService.approveAccommodation(id, approvedBy);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Отклонить размещение", description = "Отклонение размещения администратором")
    @PostMapping("/{id}/reject")
    public ResponseEntity<AccommodationResponse> rejectAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        log.info("Rejecting accommodation with ID: {}", id);
        AccommodationResponse response = accommodationService.rejectAccommodation(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить размещения владельца", description = "Получение всех размещений определенного владельца")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<AccommodationResponse>> getAccommodationsByOwner(
            @Parameter(description = "ID владельца")
            @PathVariable String ownerId) {
        log.info("Fetching accommodations for owner: {}", ownerId);
        List<AccommodationResponse> response = accommodationService.getAccommodationsByOwner(ownerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить размещения на модерации", description = "Получение всех размещений, ожидающих одобрения")
    @GetMapping("/pending")
    public ResponseEntity<List<AccommodationResponse>> getPendingAccommodations() {
        log.info("Fetching pending accommodations");
        List<AccommodationResponse> response = accommodationService.getPendingAccommodations();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить одобренные размещения", description = "Получение всех одобренных размещений")
    @GetMapping("/approved")
    public ResponseEntity<List<AccommodationResponse>> getApprovedAccommodations() {
        log.info("Fetching approved accommodations");
        List<AccommodationResponse> response = accommodationService.getApprovedAccommodations();
        return ResponseEntity.ok(response);
    }
}
