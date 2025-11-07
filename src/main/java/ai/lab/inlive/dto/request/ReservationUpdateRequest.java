package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление статуса бронирования (для SUPER_MANAGER)")
public class ReservationUpdateRequest {
    @NotNull(message = "Status is required")
    @Schema(description = "Новый статус бронирования (APPROVED - принять, REJECTED - отказать)",
            example = "APPROVED",
            required = true,
            allowableValues = {"APPROVED", "REJECTED"})
    private ReservationStatus status;
}

