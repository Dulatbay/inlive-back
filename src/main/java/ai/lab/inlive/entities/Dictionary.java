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
    @Column(nullable = false)
    private DictionaryKey key;

    // todo: create paginated response(base class for extends) for some queries (KZH, WONDER)

    @Column(nullable = false)
    private String value;
}