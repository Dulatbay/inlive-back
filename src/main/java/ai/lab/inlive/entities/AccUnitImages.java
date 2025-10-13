package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "acc_unit_images")
public class AccUnitImages extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_unit_id")
    private AccommodationUnit unit;

    @Column(name = "image_url", nullable = false, unique = true)
    private String imageUrl;
}
