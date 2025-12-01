package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление профиля пользователя")
public class UpdateUserProfileRequest {
    @NotBlank(message = "Имя не может быть пустым")
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    @Schema(description = "Email пользователя", example = "ivan@example.com")
    private String email;
}
