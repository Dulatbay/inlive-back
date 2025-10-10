package ai.lab.inlive.entities;

import ai.lab.inlive.enums.UnitType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "acc_search_request_unit_type")
public class AccSearchRequestUnitType extends AbstractEntity<Long> {


    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_s_r_id")
    private AccSearchRequest searchRequest;


    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    private UnitType unitType;
}