package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Информация о районе")
public class DistrictResponse {
    @Schema(description = "ID района", example = "1")
    private Long id;

    @Schema(description = "Название района", example = "Алмалинский район")
    private String name;

    @Schema(description = "ID города", example = "1")
    private Long cityId;

    @Schema(description = "Средняя цена за жилье в районе (в тенге)", example = "25000.0")
    private Double averagePrice;
}

