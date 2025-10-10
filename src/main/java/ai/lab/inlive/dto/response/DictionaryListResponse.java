package ai.lab.inlive.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DictionaryListResponse {
    private List<DictionaryResponse> dictionaries;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
