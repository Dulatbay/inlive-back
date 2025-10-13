package ai.lab.inlive.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DictionaryResponse {
    private Long id;
    private String key;
    private String value;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
