package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление статуса бронирования (для SUPER_MANAGER)")
public class ReservationUpdateRequest {
    @NotNull(message = "{validation.reservation.status.required}")
    @Schema(description = "Новый статус бронирования (APPROVED - принять, REJECTED - отказать)",
            example = "APPROVED",
            allowableValues = {"APPROVED", "REJECTED"})
    private ReservationStatus status;
}

