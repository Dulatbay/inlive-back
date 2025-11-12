package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "acc_images")
public class AccImages extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @Column(name = "image_url", nullable = false, unique = true)
    private String imageUrl;
}
