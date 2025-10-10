package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DistrictCreateRequest {
    @NotNull(message = "City ID is required")
    private Long cityId;

    @NotBlank(message = "District name is required")
    @Size(max = 255, message = "District name must not exceed 255 characters")
    private String name;
}
