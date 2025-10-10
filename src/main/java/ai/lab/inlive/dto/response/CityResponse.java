package ai.lab.inlive.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CityResponse {
    private Long id;
    private String name;
    private List<Long> districtIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
