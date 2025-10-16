package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.DictionaryKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictionaryCreateRequest {
    @NotBlank(message = "Dictionary key is required")
    private DictionaryKey key;

    @NotBlank(message = "Dictionary value is required")
    @Size(max = 255, message = "Dictionary value must not exceed 255 characters")
    private String value;
}
