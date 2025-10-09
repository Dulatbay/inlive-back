package ai.lab.inlive.entities;

import ai.lab.inlive.enums.UnitType;
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
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    private String name;

    @Column(columnDefinition = "text")
    private String description;

    private Integer capacity;
    private Double area;
    private Integer floor;
    private Boolean isAvailable;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccUnitImages> images = new ArrayList<>();

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccUnitDictionary> dictionaries = new ArrayList<>();

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccUnitTariffs> tariffs = new ArrayList<>();
}
