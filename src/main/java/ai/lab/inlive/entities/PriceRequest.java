package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.ClientResponseStatus;
import ai.lab.inlive.entities.enums.PriceRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "price_request")
public class PriceRequest extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_search_request_id")
    private AccSearchRequest searchRequest;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_unit_id")
    private AccommodationUnit unit;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_response_status", nullable = false)
    private ClientResponseStatus clientResponseStatus;
}
