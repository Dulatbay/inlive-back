package ai.lab.inlive.entities;

import ai.lab.inlive.enums.RangeType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_unit_tariffs")
public class AccUnitTariffs extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_unit_id")
    private AccommodationUnit unit;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    @Enumerated(EnumType.STRING)
    private RangeType rangeTypeEnum;

    @ManyToOne
    @JoinColumn(name = "range_dictionary_id")
    private Dictionary rangeType;
}
