package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с информацией о бронировании")
public class ReservationResponse {
    @Schema(description = "ID бронирования", example = "1")
    private Long id;

    @Schema(description = "ID клиента, подтвердившего бронь", example = "1")
    private Long clientId;

    @Schema(description = "Имя клиента", example = "Иван Иванов")
    private String clientName;

    @Schema(description = "ID единицы размещения", example = "1")
    private Long accommodationUnitId;

    @Schema(description = "Название единицы размещения", example = "Люкс номер")
    private String accommodationUnitName;

    @Schema(description = "Название объекта размещения", example = "Гранд Отель")
    private String accommodationName;

    @Schema(description = "Город", example = "Алматы")
    private String city;

    @Schema(description = "Район", example = "Алмалинский район")
    private String district;

    @Schema(description = "Адрес", example = "ул. Абая 123")
    private String address;

    @Schema(description = "ID заявки на цену", example = "1")
    private Long priceRequestId;

    @Schema(description = "ID заявки на поиск жилья", example = "1")
    private Long searchRequestId;

    @Schema(description = "Финальная цена бронирования", example = "50000.0")
    private Double price;

    @Schema(description = "Статус бронирования", example = "WAITING_TO_APPROVE")
    private String status;

    @Schema(description = "Требуется ли предоплата (для MVP всегда false)", example = "false")
    private Boolean needToPay;

    @Schema(description = "Дата создания", example = "2024-11-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления", example = "2024-11-01T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Дата начала проживания", example = "2024-12-01T14:00:00")
    private LocalDateTime checkInDate;

    @Schema(description = "Дата окончания проживания", example = "2024-12-05T12:00:00")
    private LocalDateTime checkOutDate;

    @Schema(description = "Количество гостей", example = "2")
    private Integer guestCount;

    @Schema(description = "Номер телефона менеджера по размещению", example = "+77001234567")
    private String managerPhoneNumber;
}

