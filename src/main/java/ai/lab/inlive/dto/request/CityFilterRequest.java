package ai.lab.inlive.dto.request;

import lombok.Data;

@Data
public class CityFilterRequest {
    private Boolean isDeleted;
    private String name;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    public CityFilterRequest() {
        // Default constructor
    }

    public CityFilterRequest(Boolean isDeleted, String name, Integer page, Integer size,
                           String sortBy, String sortDirection) {
        this.isDeleted = isDeleted;
        this.name = name;
        this.page = (page == null || page < 0) ? 0 : page;
        this.size = (size == null || size <= 0) ? 20 : size;
        this.sortBy = (sortBy == null) ? "id" : sortBy;
        this.sortDirection = (sortDirection == null) ? "asc" : sortDirection;
    }
}
