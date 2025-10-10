package ai.lab.inlive.entities;

import ai.lab.inlive.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "reservation")
public class Reservation extends AbstractEntity<Long> {
    @Column(name = "client_id", length = 64)
    private String clientId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_unit_id")
    private AccommodationUnit unit;

    @ManyToOne
    @JoinColumn(name = "price_request_id")
    private PriceRequest priceRequest;

    @ManyToOne
    @JoinColumn(name = "search_request_id")
    private AccSearchRequest searchRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "need_to_pay")
    private Boolean needToPay;
}