package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.services.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список районов успешно получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DistrictResponse>> getAllDistricts() {
        log.info("Fetching all districts");
        return ResponseEntity.ok(districtService.getAllDistricts());
    }

    @Operation(summary = "Получить районы по городу", description = "Получение всех районов определенного города")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список районов успешно получен"),
            @ApiResponse(responseCode = "404", description = "Город не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/by-city/{cityId}")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByCity(
            @Parameter(description = "ID города")
            @PathVariable Long cityId) {
        log.info("Fetching districts for city ID: {}", cityId);
        return ResponseEntity.ok(districtService.getDistrictsByCity(cityId));
    }
}
