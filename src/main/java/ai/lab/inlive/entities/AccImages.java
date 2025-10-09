package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_images")
public class AccImages extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @Column(name = "image_url", nullable = false, unique = true)
    private String imageUrl;
}
