package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccommodationUpdateRequest {
    private Long cityId;

    private Long districtId;

    @Size(max = 255, message = "{validation.accommodation.address.size}")
    private String address;

    @Size(max = 255, message = "{validation.accommodation.name.size}")
    private String name;

    @Size(max = 5000, message = "{validation.accommodation.description.size}")
    private String description;

    private Double rating;
}
