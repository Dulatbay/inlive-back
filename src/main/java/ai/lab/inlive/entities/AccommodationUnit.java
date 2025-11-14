package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.UnitType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "accommodation_units")
public class AccommodationUnit extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    private UnitType unitType;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text", nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    private Double area;

    private Integer floor;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = Boolean.TRUE;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccUnitImages> images = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccUnitDictionary> dictionaries = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccUnitTariffs> tariffs = new HashSet<>();
}
