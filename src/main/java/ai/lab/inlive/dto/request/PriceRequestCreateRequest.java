package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание заявки цены")
public class PriceRequestCreateRequest {
    @NotNull(message = "{validation.priceRequest.searchRequestId.required}")
    @Schema(description = "ID заявки на поиск жилья", example = "1")
    private Long searchRequestId;

    @NotNull(message = "{validation.priceRequest.accommodationUnitId.required}")
    @Schema(description = "ID единицы размещения", example = "1")
    private Long accommodationUnitId;

    @NotNull(message = "{validation.priceRequest.price.required}")
    @DecimalMin(value = "0.0", message = "{validation.priceRequest.price.decimalMin}")
    @Schema(description = "Предлагаемая цена", example = "50000.0")
    private Double price;
}

