package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.UnitType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
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
    private Boolean isAvailable;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccUnitImages> images = new ArrayList<>();

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccUnitDictionary> dictionaries = new ArrayList<>();

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccUnitTariffs> tariffs = new ArrayList<>();
}
