package ai.lab.inlive.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "dictionaries")
public class Dictionary extends AbstractEntity<Long> {
    @Column(nullable = false)
    private String key; // todo: enum

    // todo: transform all fields to snake case
    // todo: create paginated response(base class for extends) for some queries (KZH, WONDER)

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private String type;
}