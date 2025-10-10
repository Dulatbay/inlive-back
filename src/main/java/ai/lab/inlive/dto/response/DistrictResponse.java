package ai.lab.inlive.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DistrictResponse {
    private Long id;
    private Long cityId;
    private String cityName;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
