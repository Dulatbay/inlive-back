package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DistrictUpdateRequest {
    private Long cityId;

    @Size(max = 255, message = "District name must not exceed 255 characters")
    private String name;
}
