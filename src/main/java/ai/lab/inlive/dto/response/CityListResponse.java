package ai.lab.inlive.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CityListResponse {
    private List<CityResponse> cities;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
