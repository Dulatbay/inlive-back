package ai.lab.inlive.controllers;

import ai.lab.inlive.dto.request.UserAuthRequest;
import ai.lab.inlive.dto.response.AuthResponse;
import ai.lab.inlive.exceptions.BadRequestException;
import ai.lab.inlive.security.keycloak.KeycloakBaseUser;
import ai.lab.inlive.security.keycloak.KeycloakRole;
import ai.lab.inlive.services.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserAuthRequest userAuthRequest, HttpServletResponse response) {
        log.info("Login attempt for user: {}", userAuthRequest.email());
        AuthResponse authResponse = keycloakService.getAuthResponse(userAuthRequest.email(), userAuthRequest.password(), response);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя CLIENT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или пользователь уже существует"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(value = "/client/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid KeycloakBaseUser registrationRequest, HttpServletResponse response) {
        log.info("Registration attempt for user: {}", registrationRequest.getEmail());
        AuthResponse authResponse = keycloakService.registerUser(registrationRequest, KeycloakRole.CLIENT, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Регистрация менеджера", description = "Регистрация нового пользователя MANAGER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Менеджер успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса или пользователь уже существует"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(value = "/manager/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> registerManager(@RequestBody @Valid KeycloakBaseUser registrationRequest, HttpServletResponse response) {
        log.info("Manager registration attempt for user: {}", registrationRequest.getEmail());
        AuthResponse authResponse = keycloakService.registerUser(registrationRequest, KeycloakRole.SUPER_MANAGER, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Обновление токена", description = "Получение нового access token с помощью refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Невалидный или истекший refresh token"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Token refresh attempt (from body)");
        return ResponseEntity.status(HttpStatus.OK).body(keycloakService.refreshToken(request, response));
    }

    @PostMapping(value = "/logout")
    @Operation(summary = "Выход пользователя", description = "Выход пользователя из системы и аннулирование токенов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешный выход из системы"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout attempt");
        keycloakService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}