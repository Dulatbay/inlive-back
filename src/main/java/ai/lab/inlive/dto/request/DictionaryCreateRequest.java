package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.DictionaryKey;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictionaryCreateRequest {
    @NotNull(message = "{validation.dictionary.key.required}")
    private DictionaryKey key;

    @NotBlank(message = "{validation.dictionary.value.required}")
    @Size(max = 255, message = "{validation.dictionary.value.size}")
    private String value;
}
