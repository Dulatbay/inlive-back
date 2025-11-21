package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccommodationUnitUpdateRequest {
    private UnitType unitType;

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Double area;

    private Integer floor;

    private Boolean isAvailable;
}

