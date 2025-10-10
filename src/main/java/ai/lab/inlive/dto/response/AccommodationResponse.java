package ai.lab.inlive.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccommodationResponse {
    private Long id;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String address;
    private String name;
    private String description;
    private Double rating;
    private Boolean approved;
    private String approvedBy;
    private String ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
