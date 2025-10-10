package ai.lab.inlive.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AccommodationListResponse {
    private List<AccommodationResponse> accommodations;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
