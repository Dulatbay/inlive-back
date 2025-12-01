package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.ClientResponseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос клиента на ответ по заявке цены (ACCEPT или REJECT)")
public class PriceRequestClientResponseRequest {
    @NotNull(message = "{validation.priceRequest.clientResponseStatus.required}")
    @Schema(description = "Ответ клиента на заявку цены (ACCEPTED - принять, REJECTED - отказать)",
            example = "ACCEPTED",
            allowableValues = {"ACCEPTED", "REJECTED"})
    private ClientResponseStatus clientResponseStatus;
}

