package ai.lab.inlive.entities;

import ai.lab.inlive.enums.SearchRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "acc_search_request")
public class AccSearchRequest extends AbstractEntity<Long> {
    @Column(length = 64)
    private String author;

    private Double fromRating;
    private Double toRating;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    @Column(name = "one_night")
    private Boolean oneNight;

    private Double price;

    @Column(name = "count_of_people")
    private Integer countOfPeople;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchRequestStatus status;

    @OneToMany(mappedBy = "searchRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccSearchRequestUnitType> unitTypes = new ArrayList<>();

    @OneToMany(mappedBy = "searchRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccSearchRequestDictionary> dictionaries = new ArrayList<>();

    @OneToMany(mappedBy = "searchRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccSearchRequestDistrict> districts = new ArrayList<>();
}