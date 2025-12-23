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
@Table(name = "districts")
public class District extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Accommodation> accommodations = new HashSet<>();
}
