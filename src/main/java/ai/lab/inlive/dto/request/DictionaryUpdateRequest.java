package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.DictionaryKey;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictionaryUpdateRequest {
    @NotNull(message = "{validation.dictionary.key.required}")
    private DictionaryKey key;

    @Size(max = 255, message = "{validation.dictionary.value.size}")
    private String value;
}
