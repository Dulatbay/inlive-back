package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "reservation")
public class Reservation extends AbstractEntity<Long> {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User approvedBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_unit_id")
    private AccommodationUnit unit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "price_request_id")
    private PriceRequest priceRequest;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "search_request_id")
    private AccSearchRequest searchRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "is_need_to_pay")
    private Boolean needToPay = false;
}