package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с информацией о заявке цены")
public class PriceRequestResponse {
    @Schema(description = "ID заявки цены", example = "1")
    private Long id;

    @Schema(description = "ID заявки на поиск жилья", example = "1")
    private Long searchRequestId;

    @Schema(description = "ID единицы размещения", example = "1")
    private Long accommodationUnitId;

    @Schema(description = "Название единицы размещения", example = "Люкс номер")
    private String accommodationUnitName;

    @Schema(description = "Название объекта размещения", example = "Гранд Отель")
    private String accommodationName;

    @Schema(description = "Цена", example = "50000.0")
    private Double price;

    @Schema(description = "Статус заявки цены", example = "ACCEPTED")
    private String status;

    @Schema(description = "Статус ответа клиента", example = "WAITING")
    private String clientResponseStatus;

    @Schema(description = "Дата создания", example = "2024-11-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления", example = "2024-11-01T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Является ли запись удаленной", example = "false")
    private Boolean isDeleted;
}
