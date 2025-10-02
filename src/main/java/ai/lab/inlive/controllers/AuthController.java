package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.RefreshTokenRequest;
import ai.lab.inlive.dto.request.UserAuthRequest;
import ai.lab.inlive.dto.request.UserRegistrationRequest;
import ai.lab.inlive.dto.response.AuthResponse;
import ai.lab.inlive.services.KeycloakService;
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
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API для аутентификации и регистрации пользователей")
public class AuthController {

    private final KeycloakService keycloakService;

    @Operation(summary = "Вход пользователя", description = "Аутентификация пользователя по email и паролю")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserAuthRequest userAuthRequest) {
        log.info("Login attempt for user: {}", userAuthRequest.email());
        AuthResponse authResponse = keycloakService.getAuthResponse(userAuthRequest.email(), userAuthRequest.password());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя (CLIENT или SUPER_MANAGER)")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationRequest registrationRequest) {
        log.info("Registration attempt for user: {} with role: {}", registrationRequest.email(), registrationRequest.role());
        AuthResponse authResponse = keycloakService.registerUser(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Обновление токена", description = "Получение нового access token с помощью refresh token")
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("Token refresh attempt");
        var authResponse = keycloakService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }
}