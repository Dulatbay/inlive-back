package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание бронирования (создается автоматически после подтверждения клиентом)")
public class ReservationCreateRequest {
    @NotNull(message = "{validation.reservation.priceRequestId.required}")
    @Schema(description = "ID заявки на цену", example = "1")
    private Long priceRequestId;
}

