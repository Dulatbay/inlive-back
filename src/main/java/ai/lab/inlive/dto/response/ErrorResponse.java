package ai.lab.inlive.dto.response;

import lombok.Data;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static ai.lab.inlive.constants.ValueConstants.ZONE_ID;

@Data
public class ErrorResponse {
    private String error;

    private String message;

    private String stackTrace;

    private long timestamp;

    private String requestId;

    public ErrorResponse(String error, String message, String stackTrace) {
        this.error = error;
        this.message = message;
        this.stackTrace = stackTrace;

        this.timestamp = LocalDateTime.now(ZONE_ID).atZone(ZONE_ID).toEpochSecond();

        this.requestId = MDC.get("traceId");
    }
}