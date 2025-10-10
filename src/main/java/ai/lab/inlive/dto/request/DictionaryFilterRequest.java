package ai.lab.inlive.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DictionaryFilterRequest {
    private String type;
    private Boolean isDeleted;
    private String key;
    private String value;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    public DictionaryFilterRequest(String type, Boolean isDeleted, String key, String value,
                                 Integer page, Integer size, String sortBy, String sortDirection) {
        this.type = type;
        this.isDeleted = isDeleted;
        this.key = key;
        this.value = value;
        this.page = (page == null || page < 0) ? 0 : page;
        this.size = (size == null || size <= 0) ? 20 : size;
        this.sortBy = (sortBy == null) ? "id" : sortBy;
        this.sortDirection = (sortDirection == null) ? "asc" : sortDirection;
    }
}
