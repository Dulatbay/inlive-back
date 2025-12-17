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
@Table(name = "cities")
public class City extends AbstractEntity<Long>{
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<District> districts = new HashSet<>();

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Accommodation> accommodations = new HashSet<>();
}
