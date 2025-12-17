package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@BatchSize(size = 50)
@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "accommodations")
public class Accommodation extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Column(columnDefinition = "text", nullable = false)
    private String address;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text", nullable = false)
    private String description;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "is_approved")
    private Boolean approved;

    @ManyToOne(fetch = FetchType.LAZY) // optional = true,
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User ownerId;

    @BatchSize(size = 50)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccImages> images = new HashSet<>();

    @BatchSize(size = 50)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccDocuments> documents = new HashSet<>();

    @BatchSize(size = 50)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccDictionary> dictionaries = new HashSet<>();

    @BatchSize(size = 50)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccConfig> configs = new HashSet<>();

    @BatchSize(size = 50)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccommodationUnit> units = new HashSet<>();
}
