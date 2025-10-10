package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.CityCreateRequest;
import ai.lab.inlive.dto.request.CityFilterRequest;
import ai.lab.inlive.dto.request.CityUpdateRequest;
import ai.lab.inlive.dto.response.CityListResponse;
import ai.lab.inlive.dto.response.CityResponse;
import ai.lab.inlive.services.CityService;
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
@RequestMapping("/cities")
@Tag(name = "City", description = "API для работы с городами")
public class CityController {
    private final CityService cityService;

    @Operation(summary = "Получить все города", description = "Получение списка всех городов")
    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        log.info("Fetching all cities");
        List<CityResponse> response = cityService.getAllCities();
        return ResponseEntity.ok(response);
    }
}
