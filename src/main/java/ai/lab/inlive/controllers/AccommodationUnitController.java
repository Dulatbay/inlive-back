package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.security.authorization.AccessForAdminsAndSuperManagers;
import ai.lab.inlive.services.AccommodationUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodation-units")
@Tag(name = "Accommodation Unit", description = "API для работы с единицами размещения")
public class AccommodationUnitController {
    private final AccommodationUnitService accommodationUnitService;

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Создать единицу размещения", description = "Создание новой квартиры/номера")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createUnit(@RequestBody @Valid AccommodationUnitCreateRequest request) {
        log.info("Creating accommodation unit for accommodation {}", request.getAccommodationId());
        accommodationUnitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @AccessForAdminsAndSuperManagers
    @Operation(summary = "Добавить тариф к единице размещения", description = "Прикрепление тарифа к квартире/номеру")
    @PostMapping(value = "/{unitId}/tariffs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccUnitTariffResponse> addTariff(
            @PathVariable Long unitId,
            @RequestBody @Valid AccUnitTariffCreateRequest request) {
        log.info("Adding tariff to unit {}", unitId);
        AccUnitTariffResponse response = accommodationUnitService.addTariff(unitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
