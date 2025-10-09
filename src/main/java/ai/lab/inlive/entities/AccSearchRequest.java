package ai.lab.inlive.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "acc_search_request")
public class AccSearchRequest extends AbstractEntity<Long> {
    private String author;
    private Double fromRating;
    private Double toRating;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}