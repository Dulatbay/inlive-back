package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AccommodationUnitCreateRequest {
    @NotNull(message = "Accommodation ID is required")
    private Long accommodationId;

    @NotNull(message = "Unit type is required")
    private UnitType unitType;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Double area;

    private Integer floor;

    private List<Long> serviceDictionaryIds;

    private List<Long> conditionDictionaryIds;

    private List<MultipartFile> images;
}
