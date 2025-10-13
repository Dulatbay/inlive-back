package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_unit_dictionary")
public class AccUnitDictionary extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_unit_id")
    private AccommodationUnit unit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id")
    private Dictionary dictionary;
}
