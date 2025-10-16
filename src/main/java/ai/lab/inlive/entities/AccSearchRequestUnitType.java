package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.UnitType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "acc_search_request_unit_type")
public class AccSearchRequestUnitType extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "search_request_id", nullable = false)
    private AccSearchRequest searchRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    private UnitType unitType;
}