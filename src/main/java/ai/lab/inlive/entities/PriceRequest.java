package ai.lab.inlive.entities;

import ai.lab.inlive.enums.ClientResponseStatus;
import ai.lab.inlive.enums.PriceRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "price_request")
public class PriceRequest extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_search_req_id")
    private AccSearchRequest searchRequest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_unit_id")
    private AccommodationUnit unit;

    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_response_status", nullable = false)
    private ClientResponseStatus clientResponseStatus;
}
