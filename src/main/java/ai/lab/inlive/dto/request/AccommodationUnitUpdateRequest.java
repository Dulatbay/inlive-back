package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccommodationUnitUpdateRequest {
    private UnitType unitType;

    @Size(max = 255, message = "{validation.accommodationUnit.name.size}")
    private String name;

    @Size(max = 5000, message = "{validation.accommodationUnit.description.size}")
    private String description;

    @Min(value = 1, message = "{validation.accommodationUnit.capacity.min}")
    private Integer capacity;

    private Double area;

    private Integer floor;

    private Boolean isAvailable;
}

