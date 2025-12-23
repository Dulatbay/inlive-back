package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.UnitType;
import ai.lab.inlive.validators.ValidFiles;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AccommodationUnitCreateRequest {
    @NotNull(message = "{validation.accommodationUnit.accommodationId.required}")
    private Long accommodationId;

    @NotNull(message = "{validation.accommodationUnit.unitType.required}")
    private UnitType unitType;

    @NotBlank(message = "{validation.accommodationUnit.name.required}")
    @Size(max = 255, message = "{validation.accommodationUnit.name.size}")
    private String name;

    @NotBlank(message = "{validation.accommodationUnit.description.required}")
    @Size(max = 5000, message = "{validation.accommodationUnit.description.size}")
    private String description;

    @NotNull(message = "{validation.accommodationUnit.capacity.required}")
    @Min(value = 1, message = "{validation.accommodationUnit.capacity.min}")
    private Integer capacity;

    private Double area;

    private Integer floor;

    private List<Long> serviceDictionaryIds;

    private List<Long> conditionDictionaryIds;

    @ValidFiles
    private List<MultipartFile> images;
}
