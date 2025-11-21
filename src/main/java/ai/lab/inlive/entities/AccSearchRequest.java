package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.SearchRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "acc_search_request")
public class AccSearchRequest extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

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
    private Set<AccSearchRequestUnitType> unitTypes = new HashSet<>();

    @OneToMany(mappedBy = "searchRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccSearchRequestDictionary> dictionaries = new HashSet<>();

    @OneToMany(mappedBy = "searchRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccSearchRequestDistrict> districts = new HashSet<>();
}