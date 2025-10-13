package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.SearchRequestStatus;
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
    @Column(length = 64, nullable = false)
    private String author;

    @Column(name = "from_rating")
    private Double fromRating;

    @Column(name = "to_rating")
    private Double toRating;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    @Column(name = "one_night")
    private Boolean oneNight;

    @Column(nullable = false)
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