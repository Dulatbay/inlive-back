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
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var createdByUserId = Utils.extractIdFromToken(token);

        accommodationService.createAccommodation(request, createdByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Получить размещение по ID", description = "Получение размещения по идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationResponse> getAccommodationById(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
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
        accommodationService.updateAccommodation(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить размещение", description = "Мягкое удаление размещения")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Одобрить размещение", description = "Одобрение размещения администратором")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveAccommodation(
            @Parameter(description = "ID размещения", example = "1")
            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var approvedByUserId = Utils.extractIdFromToken(token);

        accommodationService.approveAccommodation(id, approvedByUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отклонить размещение", description = "Отклонение размещения администратором")
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
}
