package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание заявки цены")
public class PriceRequestCreateRequest {
    @NotNull(message = "Search request ID is required")
    @Schema(description = "ID заявки на поиск жилья", example = "1", required = true)
    private Long searchRequestId;

    @NotNull(message = "Accommodation unit ID is required")
    @Schema(description = "ID единицы размещения", example = "1", required = true)
    private Long accommodationUnitId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    @Schema(description = "Предлагаемая цена", example = "50000.0", required = true)
    private Double price;
}

