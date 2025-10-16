package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.services.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/districts")
@Tag(name = "District", description = "API для работы с районами")
public class DistrictController {
    private final DistrictService districtService;

    @Operation(summary = "Получить все районы", description = "Получение списка всех районов")
    @GetMapping
    public ResponseEntity<List<DistrictResponse>> getAllDistricts() {
        log.info("Fetching all districts");
        List<DistrictResponse> response = districtService.getAllDistricts();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить районы по городу", description = "Получение всех районов определенного города")
    @GetMapping("/by-city/{cityId}")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByCity(
            @Parameter(description = "ID города")
            @PathVariable Long cityId) {
        log.info("Fetching districts for city ID: {}", cityId);
        List<DistrictResponse> response = districtService.getDistrictsByCity(cityId);
        return ResponseEntity.ok(response);
    }
}
