package ai.lab.inlive.dto.request;

import lombok.Data;

@Data
public class AccommodationFilterRequest {
    private Long cityId;
    private Long districtId;
    private Boolean approved;
    private String ownerId;
    private Double minRating;
    private Boolean isDeleted;
    private String name;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    public AccommodationFilterRequest() {
        // Default constructor
    }

    public AccommodationFilterRequest(Long cityId, Long districtId, Boolean approved, String ownerId,
                                    Double minRating, Boolean isDeleted, String name, Integer page,
                                    Integer size, String sortBy, String sortDirection) {
        this.cityId = cityId;
        this.districtId = districtId;
        this.approved = approved;
        this.ownerId = ownerId;
        this.minRating = minRating;
        this.isDeleted = isDeleted;
        this.name = name;
        this.page = (page == null || page < 0) ? 0 : page;
        this.size = (size == null || size <= 0) ? 20 : size;
        this.sortBy = (sortBy == null) ? "id" : sortBy;
        this.sortDirection = (sortDirection == null) ? "asc" : sortDirection;
    }
}
