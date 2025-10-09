package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_unit_images")
public class AccUnitImages extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_unit_id")
    private AccommodationUnit unit;

    @Column(name = "image_url", nullable = false, unique = true)
    private String imageUrl;
}
