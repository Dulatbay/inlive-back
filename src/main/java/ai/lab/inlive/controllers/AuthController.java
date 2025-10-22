package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.UserAuthRequest;
import ai.lab.inlive.dto.response.AuthResponse;
import ai.lab.inlive.security.keycloak.KeycloakBaseUser;
import ai.lab.inlive.security.keycloak.KeycloakRole;
import ai.lab.inlive.services.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserAuthRequest userAuthRequest, HttpServletResponse response) {
        log.info("Login attempt for user: {}", userAuthRequest.email());
        AuthResponse authResponse = keycloakService.getAuthResponse(userAuthRequest.email(), userAuthRequest.password(), response);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя CLIENT")
    @PostMapping(value = "/client/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid KeycloakBaseUser registrationRequest, HttpServletResponse response) {
        log.info("Registration attempt for user: {}", registrationRequest.getEmail());
        keycloakService.createUserByRole(registrationRequest, KeycloakRole.CLIENT);
        AuthResponse authResponse = keycloakService.getAuthResponse(registrationRequest.getEmail(), registrationRequest.getPassword(), response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Регистрация менеджера", description = "Регистрация нового пользователя MANAGER")
    @PostMapping(value = "/manager/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> registerManager(@RequestBody @Valid KeycloakBaseUser registrationRequest, HttpServletResponse response) {
        log.info("Manager registration attempt for user: {}", registrationRequest.getEmail());
        keycloakService.createUserByRole(registrationRequest, KeycloakRole.SUPER_MANAGER);
        AuthResponse authResponse = keycloakService.getAuthResponse(registrationRequest.getEmail(), registrationRequest.getPassword(), response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Обновление токена", description = "Получение нового access token с помощью refresh token")
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Token refresh attempt (from body)");
        return ResponseEntity.status(HttpStatus.OK).body(keycloakService.refreshToken(request, response));
    }

    @PostMapping(value = "/logout")
    @Operation(summary = "Выход пользователя", description = "Выход пользователя из системы и аннулирование токенов")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout attempt");
        keycloakService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}