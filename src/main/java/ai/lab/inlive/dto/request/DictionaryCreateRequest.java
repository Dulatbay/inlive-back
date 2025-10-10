package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictionaryCreateRequest {
    @NotBlank(message = "Dictionary key is required")
    @Size(max = 255, message = "Dictionary key must not exceed 255 characters")
    private String key;

    @NotBlank(message = "Dictionary value is required")
    @Size(max = 255, message = "Dictionary value must not exceed 255 characters")
    private String value;

    @NotBlank(message = "Dictionary type is required")
    @Size(max = 255, message = "Dictionary type must not exceed 255 characters")
    private String type;
}
