package ai.lab.inlive.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DistrictListResponse {
    private List<DistrictResponse> districts;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
