package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.base.PaginatedResponse;
import ai.lab.inlive.dto.params.AccommodationSearchParams;
import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUpdateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.AccommodationService;
import ai.lab.inlive.constants.Utils;
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

import java.util.List;

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
        accommodationService.createAccommodation(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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

    @Operation(summary = "Получить все размещения, соответствующие фильтрам", description = "Получение списка размещений с возможностью фильтрации")
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<AccommodationResponse>> searchAccommodations(
            @ModelAttribute AccommodationSearchParams accommodationSearchParams,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        log.info("Searching accommodations with filters - page: {}, size: {}, sortBy: {}, sortDirection: {}, filters: {}",
                page, size, sortBy, sortDirection, accommodationSearchParams);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy))
        );
        Page<AccommodationResponse> response = accommodationService.searchWithParams(accommodationSearchParams, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @Operation(summary = "Обновить размещение", description = "Обновление данных размещения")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AccommodationUpdateRequest request) {
        log.info("Updating accommodation with ID: {}", id);
        accommodationService.updateAccommodation(id, request);
        return ResponseEntity.ok().build();
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
            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var approvedByUserId = Utils.extractIdFromToken(token);
        log.info("Approving accommodation with ID: {} by user: {}", id, approvedByUserId);
        AccommodationResponse response = accommodationService.approveAccommodation(id, approvedByUserId);
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
            @PathVariable Long ownerId) {
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
