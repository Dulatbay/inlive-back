package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "accommodations")
public class Accommodation extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(optional = false)
    @JoinColumn(name = "district_id")
    private District district;

    private String address;
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    private Double rating;
    private Boolean approved;
    private String approvedBy;
    private String ownerId;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccImages> images = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccDocuments> documents = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccDictionary> dictionaries = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccConfig> configs = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccommodationUnit> units = new ArrayList<>();
}
