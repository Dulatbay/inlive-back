package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Ответ с информацией о заявке на поиск жилья")
public class AccSearchRequestResponse {
    @Schema(description = "ID заявки", example = "1")
    private Long id;

    @Schema(description = "ID автора заявки", example = "1")
    private Long authorId;

    @Schema(description = "Имя автора заявки", example = "Иван Иванов")
    private String authorName;

    @Schema(description = "Минимальный рейтинг", example = "4.5")
    private Double fromRating;

    @Schema(description = "Максимальный рейтинг", example = "5.0")
    private Double toRating;

    @Schema(description = "Дата заезда (check-in)", example = "2024-12-01")
    private LocalDate checkInDate;

    @Schema(description = "Дата выезда (check-out)", example = "2024-12-05")
    private LocalDate checkOutDate;

    @Schema(description = "На одну ночь", example = "false")
    private Boolean oneNight;

    @Schema(description = "Желаемая цена", example = "50000.0")
    private Double price;

    @Schema(description = "Количество человек", example = "2")
    private Integer countOfPeople;

    @Schema(description = "Статус заявки", example = "OPEN_TO_PRICE_REQUEST")
    private String status;

    @Schema(description = "Список типов недвижимости")
    private List<String> unitTypes;

    @Schema(description = "Список районов")
    private List<DistrictResponse> districts;

    @Schema(description = "Список требуемых услуг")
    private List<DictionaryResponse> services;

    @Schema(description = "Список требуемых условий")
    private List<DictionaryResponse> conditions;

    @Schema(description = "Дата создания заявки", example = "2024-11-01T10:00:00")
    private LocalDateTime createdAt;
}

