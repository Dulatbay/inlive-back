package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CityUpdateRequest {
    @Size(max = 255, message = "City name must not exceed 255 characters")
    private String name;
}
