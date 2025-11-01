package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.DictionaryKey;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "dictionaries")
public class Dictionary extends AbstractEntity<Long> {
    @Enumerated(EnumType.STRING)
    @Column(name = "\"key\"", nullable = false)
    private DictionaryKey key;

    @Column(nullable = false)
    private String value;
}