package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_config")
public class AccConfig extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dictionary_id")
    private Dictionary dictionary;

    @Column(name = "config_value", nullable = false)
    private String configValue;
}
