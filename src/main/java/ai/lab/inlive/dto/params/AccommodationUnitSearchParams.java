package ai.lab.inlive.dto.params;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class AccommodationUnitSearchParams {
    @Parameter(description = "ID размещения (accommodation)")
    private Long accommodationId;

    @Parameter(description = "Тип юнита (enum name)")
    private String unitType;

    @Parameter(description = "Доступность (is_available)")
    private Boolean isAvailable;

    @Parameter(description = "Статус удаления (true - удаленные, false - активные)")
    private Boolean isDeleted;

    @Parameter(description = "Название (поиск по части названия)")
    private String name;

    @Parameter(description = "Минимальная вместимость")
    private Integer minCapacity;

    @Parameter(description = "Максимальная вместимость")
    private Integer maxCapacity;
}

