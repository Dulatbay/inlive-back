package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Информация о справочнике")
public class DictionaryResponse {
    @Schema(description = "ID справочника", example = "1")
    private Long id;

    @Schema(description = "Ключ справочника", example = "ACC_SERVICE")
    private String key;

    @Schema(description = "Значение справочника", example = "WiFi")
    private String value;
}

