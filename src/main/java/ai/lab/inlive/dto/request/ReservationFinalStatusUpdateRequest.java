package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление финального статуса бронирования после прихода/неприхода клиента")
public class ReservationFinalStatusUpdateRequest {
    @NotNull(message = "Status is required")
    @Schema(description = "Финальный статус бронирования (FINISHED_SUCCESSFUL - клиент пришел, CLIENT_DIDNT_CAME - клиент не пришел)",
            example = "FINISHED_SUCCESSFUL",
            allowableValues = {"FINISHED_SUCCESSFUL", "CLIENT_DIDNT_CAME"})
    private ReservationStatus status;
}

