package ai.lab.inlive.entities.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    SUCCESSFUL,
    WAITING_TO_APPROVE,
    APPROVED,
    REJECTED,
    CLIENT_DIDNT_CAME,
    FINISHED_SUCCESSFUL,
    CANCELED
}
