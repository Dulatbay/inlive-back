package ai.lab.inlive.entities.enums;

import lombok.Getter;

@Getter
public enum SearchRequestStatus {
    OPEN_TO_PRICE_REQUEST,
    PRICE_REQUEST_PENDING,
    WAIT_TO_RESERVATION,
    FINISHED,
    CANCELLED,
    EXPIRED
}
