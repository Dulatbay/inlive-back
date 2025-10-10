package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictionaryUpdateRequest {
    @Size(max = 255, message = "Dictionary key must not exceed 255 characters")
    private String key;

    @Size(max = 255, message = "Dictionary value must not exceed 255 characters")
    private String value;

    @Size(max = 255, message = "Dictionary type must not exceed 255 characters")
    private String type;
}
