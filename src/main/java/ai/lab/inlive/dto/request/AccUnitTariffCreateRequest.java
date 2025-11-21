package ai.lab.inlive.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccUnitTariffCreateRequest {
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private Double price;

    // Optional, default KZT
    private String currency;

    // Dictionary ID with key RANGE_TYPE
    @NotNull(message = "Range type (dictionary id) is required")
    private Long rangeTypeId;
}
