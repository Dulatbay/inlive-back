package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.response.CityResponse;
import ai.lab.inlive.services.CityService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/cities")
@Tag(name = "City", description = "API для работы с городами")
public class CityController {
    private final CityService cityService;

    @Operation(summary = "Получить все города", description = "Получение списка всех городов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список городов успешно получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        log.info("Fetching all cities");
        List<CityResponse> response = cityService.getAllCities();
        return ResponseEntity.ok(response);
    }
}
