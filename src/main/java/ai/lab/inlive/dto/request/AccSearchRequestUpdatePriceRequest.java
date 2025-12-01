package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление цены заявки на поиск жилья")
public class AccSearchRequestUpdatePriceRequest {
    @NotNull(message = "{validation.searchRequest.price.required}")
    @DecimalMin(value = "0.0", message = "{validation.searchRequest.price.decimalMin}")
    @Schema(description = "Новая цена", example = "50000.0")
    private Double price;
}

