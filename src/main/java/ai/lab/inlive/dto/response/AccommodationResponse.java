package ai.lab.inlive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
public class AccommodationResponse {
    private Long id;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String address;
    private String name;
    private String description;
    private Double rating;
    private Boolean approved;
    private Long approvedBy;
    private Long ownerId;

    @Schema(description = "Список предоставляемых услуг")
    private Set<DictionaryResponse> services;

    @Schema(description = "Список условий проживания")
    private Set<DictionaryResponse> conditions;

    private Set<String> imageUrls;
}
