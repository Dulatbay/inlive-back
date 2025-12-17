package ai.lab.inlive.controllers;

import ai.lab.inlive.constants.Utils;
import ai.lab.inlive.dto.request.UpdateUserProfileRequest;
import ai.lab.inlive.dto.response.UserResponse;
import ai.lab.inlive.services.UserService;
import ai.lab.inlive.validators.ValidFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "API для работы с текущим пользователем")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить информацию о текущем пользователе",
            description = "Получение профиля текущего авторизованного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        UserResponse response = userService.getCurrentUser(keycloakId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновить профиль пользователя",
            description = "Обновление email, имени и фамилии текущего пользователя. Username и пароль изменению не подлежат.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateProfile(@RequestBody @Valid UpdateUserProfileRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        userService.updateUserProfile(keycloakId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Загрузить фото профиля",
            description = "Загрузка фотографии профиля текущего пользователя. Принимаются только изображения (JPEG, PNG, JPG). Максимальный размер: 10 МБ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фото успешно загружено",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Некорректный формат файла. Разрешены только изображения", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "413", description = "Размер файла превышает 10 МБ", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPhoto(@ValidFile @RequestParam("photo") MultipartFile photo) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        userService.updateUserPhoto(keycloakId, photo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить фото профиля",
            description = "Удаление фотографии профиля текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Фото успешно удалено"),
            @ApiResponse(responseCode = "400", description = "У пользователя нет фото", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @DeleteMapping("/me/photo")
    public ResponseEntity<Void> deletePhoto() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        userService.deleteUserPhoto(keycloakId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

