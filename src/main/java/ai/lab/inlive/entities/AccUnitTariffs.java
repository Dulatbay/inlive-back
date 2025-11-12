package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "acc_unit_tariffs")
public class AccUnitTariffs extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_unit_id")
    private AccommodationUnit unit;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "range_dictionary_id")
    private Dictionary rangeType;
}
