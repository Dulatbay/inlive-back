package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Запрос на создание заявки на поиск жилья (для CLIENT)")
public class AccSearchRequestCreateRequest {
    @NotNull(message = "{validation.searchRequest.checkInDate.required}")
    @FutureOrPresent(message = "{validation.searchRequest.checkInDate.futureOrPresent}")
    @Schema(description = "Дата заезда (check-in)", example = "2025-12-01")
    private LocalDate checkInDate;

    @Schema(description = "Дата выезда (check-out). Опциональна, если указан флаг oneNight", example = "2025-12-05")
    private LocalDate checkOutDate;

    @Schema(description = "Флаг 'на одну ночь'. Если true, checkOutDate = checkInDate + 1 день", example = "false")
    private Boolean oneNight;

    @NotNull(message = "{validation.searchRequest.price.required}")
    @DecimalMin(value = "0.0", message = "{validation.searchRequest.price.decimalMin}")
    @Schema(description = "Предложенная цена", example = "50000.0")
    private Double price;

    @NotNull(message = "{validation.searchRequest.countOfPeople.required}")
    @Min(value = 1, message = "{validation.searchRequest.countOfPeople.min}")
    @Schema(description = "Количество людей", example = "2")
    private Integer countOfPeople;

    @Schema(description = "Минимальный рейтинг", example = "4.0")
    private Double fromRating;

    @Schema(description = "Максимальный рейтинг", example = "5.0")
    private Double toRating;

    @NotEmpty(message = "{validation.searchRequest.unitTypes.notEmpty}")
    @Schema(description = "Типы недвижимости (HOTEL_ROOM, APARTMENT)", example = "[\"HOTEL_ROOM\", \"APARTMENT\"]")
    private List<UnitType> unitTypes;

    @NotEmpty(message = "{validation.searchRequest.districtIds.notEmpty}")
    @Schema(description = "ID районов", example = "[1, 2, 3]")
    private List<Long> districtIds;

    @Schema(description = "ID необходимых услуг (ACC_SERVICE)", example = "[1, 2, 3]")
    private List<Long> serviceDictionaryIds;

    @Schema(description = "ID условий проживания (ACC_CONDITION)", example = "[4, 5, 6]")
    private List<Long> conditionDictionaryIds;
}

