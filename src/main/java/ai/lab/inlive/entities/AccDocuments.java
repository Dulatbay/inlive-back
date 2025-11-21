package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "acc_documents")
public class AccDocuments extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id")
    private Accommodation accommodation;

    @Column(name = "document_url", nullable = false, unique = true)
    private String documentUrl;

    @Column(name = "document_type", nullable = false)
    private String documentType;
}
