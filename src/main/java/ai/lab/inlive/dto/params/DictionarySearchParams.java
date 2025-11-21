package ai.lab.inlive.dto.params;

import ai.lab.inlive.entities.enums.DictionaryKey;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.List;

@Data
public class DictionarySearchParams {
    @Parameter(description = "Статус удаления (true - удаленные, false - активные)")
    private Boolean isDeleted;

    @Parameter(description = "Ключи (поиск по части ключей)")
    private List<DictionaryKey> keys;

    @Parameter(description = "Значение (поиск по части значения)")
    private String value;
}
