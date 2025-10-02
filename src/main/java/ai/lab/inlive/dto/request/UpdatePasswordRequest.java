package ai.lab.inlive.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @JsonIgnore
    private String email;

    @NotNull(message = "Пожалуйста, укажите старый пароль")
    private String oldPassword;

    @NotNull(message = "Пожалуйста, укажите новый пароль")
    private String newPassword;
}