package ai.lab.inlive.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_unit_dictionary")
public class AccUnitDictionary extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_unit_id")
    private AccommodationUnit unit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dictionary_id")
    private Dictionary dictionary;
}
