package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccUnitTariffCreateRequest {
    @NotNull(message = "{validation.tariff.price.required}")
    @DecimalMin(value = "0.0", message = "{validation.tariff.price.decimalMin}")
    private Double price;

    // Optional, default KZT
    private String currency;

    // Dictionary ID with key RANGE_TYPE
    @NotNull(message = "{validation.tariff.rangeTypeId.required}")
    private Long rangeTypeId;
}
