package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccommodationUpdateRequest {
    private Long cityId;

    private Long districtId;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Double rating;
}
