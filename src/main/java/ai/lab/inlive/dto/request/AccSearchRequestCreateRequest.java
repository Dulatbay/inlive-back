package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Запрос на создание заявки на поиск жилья (для CLIENT)")
public class AccSearchRequestCreateRequest {

    @NotNull(message = "From date is required")
    @Future(message = "From date must be in the future")
    @Schema(description = "Дата заезда", example = "2024-12-01T14:00:00", required = true)
    private LocalDateTime fromDate;

    @NotNull(message = "To date is required")
    @Schema(description = "Дата выезда", example = "2024-12-05T12:00:00", required = true)
    private LocalDateTime toDate;

    @Schema(description = "Флаг 'на одну ночь'", example = "false")
    private Boolean oneNight;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    @Schema(description = "Предложенная цена", example = "50000.0", required = true)
    private Double price;

    @NotNull(message = "Count of people is required")
    @Min(value = 1, message = "Count of people must be at least 1")
    @Schema(description = "Количество людей", example = "2", required = true)
    private Integer countOfPeople;

    @Schema(description = "Минимальный рейтинг", example = "4.0")
    private Double fromRating;

    @Schema(description = "Максимальный рейтинг", example = "5.0")
    private Double toRating;

    @NotEmpty(message = "At least one unit type is required")
    @Schema(description = "Типы недвижимости (HOTEL_ROOM, APARTMENT)", example = "[\"HOTEL_ROOM\", \"APARTMENT\"]", required = true)
    private List<UnitType> unitTypes;

    @NotEmpty(message = "At least one district is required")
    @Schema(description = "ID районов", example = "[1, 2, 3]", required = true)
    private List<Long> districtIds;

    @Schema(description = "ID необходимых услуг (ACC_SERVICE)", example = "[1, 2, 3]")
    private List<Long> serviceDictionaryIds;

    @Schema(description = "ID условий проживания (ACC_CONDITION)", example = "[4, 5, 6]")
    private List<Long> conditionDictionaryIds;
}

