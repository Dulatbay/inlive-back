package ai.lab.inlive.dto.request;

import ai.lab.inlive.validators.ValidFiles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AccommodationCreateRequest {
    @NotNull(message = "{validation.accommodation.cityId.required}")
    private Long cityId;

    @NotNull(message = "{validation.accommodation.districtId.required}")
    private Long districtId;

    @NotBlank(message = "{validation.accommodation.address.required}")
    @Size(max = 255, message = "{validation.accommodation.address.size}")
    private String address;

    @NotBlank(message = "{validation.accommodation.name.required}")
    @Size(max = 255, message = "{validation.accommodation.name.size}")
    private String name;

    @Size(max = 5000, message = "{validation.accommodation.description.size}")
    private String description;

    @NotNull(message = "{validation.accommodation.rating.required}")
    private Double rating;

    private List<Long> serviceDictionaryIds;

    private List<Long> conditionDictionaryIds;

    @ValidFiles
    private List<MultipartFile> images;
}
