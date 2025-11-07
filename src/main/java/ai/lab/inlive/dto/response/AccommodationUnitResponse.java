package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Ответ с информацией о единице размещения")
public class AccommodationUnitResponse {
    @Schema(description = "ID единицы размещения", example = "1")
    private Long id;

    @Schema(description = "ID объекта размещения", example = "1")
    private Long accommodationId;

    @Schema(description = "Тип единицы", example = "APARTMENT")
    private String unitType; // enum name

    @Schema(description = "Название", example = "Люкс номер")
    private String name;

    @Schema(description = "Описание", example = "Просторный номер с видом на море")
    private String description;

    @Schema(description = "Вместимость (количество человек)", example = "4")
    private Integer capacity;

    @Schema(description = "Площадь в кв.м", example = "45.5")
    private Double area;

    @Schema(description = "Этаж", example = "3")
    private Integer floor;

    @Schema(description = "Доступность", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Список предоставляемых услуг")
    private List<DictionaryResponse> services;

    @Schema(description = "Список условий проживания")
    private List<DictionaryResponse> conditions;

    @Schema(description = "Список тарифов")
    private List<AccUnitTariffResponse> tariffs;
}
