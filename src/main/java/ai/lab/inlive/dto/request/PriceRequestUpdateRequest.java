package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.PriceRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление заявки цены (для SUPER_MANAGER)")
public class PriceRequestUpdateRequest {
    @NotNull(message = "{validation.priceRequest.status.required}")
    @Schema(description = "Новый статус заявки (ACCEPTED, RAISED, DECREASED)", example = "ACCEPTED")
    private PriceRequestStatus status;

    @NotNull(message = "{validation.priceRequest.price.required}")
    @DecimalMin(value = "0.0", message = "{validation.priceRequest.price.decimalMin}")
    @Schema(description = "Новая цена (может быть изменена при RAISED или DECREASED)", example = "55000.0")
    private Double price;
}

