package ai.lab.inlive.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @JsonIgnore
    private String email;

    @NotBlank(message = "Old password is required")
    @Size(min = 8, max = 255, message = "Old password must be between 8 and 255 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 255, message = "New password must be between 8 and 255 characters")
    private String newPassword;
}