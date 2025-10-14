package ai.lab.inlive.dto.params;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class DictionarySearchParams {
    @Parameter(description = "Статус удаления (true - удаленные, false - активные)")
    private Boolean isDeleted;

    @Parameter(description = "Ключ (поиск по части ключа)")
    private String key;

    @Parameter(description = "Значение (поиск по части значения)")
    private String value;
}

