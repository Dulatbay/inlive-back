package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на обновление услуг и условий для размещения")
public class AccommodationDictionariesUpdateRequest {
    @Schema(description = "Список ID услуг (ACC_SERVICE) - заменяет текущий список", example = "[1, 2, 3]")
    private List<Long> serviceDictionaryIds;

    @Schema(description = "Список ID условий (ACC_CONDITION) - заменяет текущий список", example = "[4, 5, 6]")
    private List<Long> conditionDictionaryIds;
}
