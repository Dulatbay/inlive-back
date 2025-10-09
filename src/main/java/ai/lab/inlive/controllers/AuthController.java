package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.RefreshTokenRequest;
import ai.lab.inlive.dto.request.UserAuthRequest;
import ai.lab.inlive.dto.request.UserRegistrationRequest;
import ai.lab.inlive.dto.response.AuthResponse;
import ai.lab.inlive.security.keycloak.KeycloakBaseUser;
import ai.lab.inlive.security.keycloak.KeycloakRole;
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
    @PostMapping(value = "/client/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid KeycloakBaseUser registrationRequest) {
        log.info("Registration attempt for user: {}", registrationRequest.getEmail());
        AuthResponse authResponse = keycloakService.registerUser(registrationRequest, KeycloakRole.CLIENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Обновление токена", description = "Получение нового access token с помощью refresh token")
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest body
    ) {
        log.info("Token refresh attempt (from body)");
        try {
            AuthResponse auth = keycloakService.refreshToken(body.refreshToken());
            log.info("Token refresh successful");
            return ResponseEntity.ok(auth);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(value = "/logout")
    @Operation(summary = "Выход пользователя", description = "Выход пользователя из системы и аннулирование токенов")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader, String refreshToken) {
        log.info("Logout attempt");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String accessToken = authorizationHeader.substring(7);
        try {
            keycloakService.logout(accessToken, refreshToken);
            log.info("Logout successful");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}