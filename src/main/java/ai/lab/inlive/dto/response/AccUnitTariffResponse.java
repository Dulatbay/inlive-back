package ai.lab.inlive.dto.response;

import lombok.Data;

@Data
public class AccUnitTariffResponse {
    private Long id;
    private Double price;
    private String currency;
    private Long rangeTypeId;
    private String rangeTypeKey;
    private String rangeTypeValue;
}

