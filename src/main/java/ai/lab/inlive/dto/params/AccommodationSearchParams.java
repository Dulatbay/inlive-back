package ai.lab.inlive.dto.params;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class AccommodationSearchParams {
    @Parameter(description = "ID города")
    private Long cityId;

    @Parameter(description = "ID района")
    private Long districtId;

    @Parameter(description = "Статус одобрения")
    private Boolean approved;

    @Parameter(description = "ID владельца")
    private Long ownerId;

    @Parameter(description = "Минимальный рейтинг")
    private Double minRating;

    @Parameter(description = "Статус удаления (true - удаленные, false - активные)")
    private Boolean isDeleted;

    @Parameter(description = "Название (поиск по части названия)")
    private String name;
}
