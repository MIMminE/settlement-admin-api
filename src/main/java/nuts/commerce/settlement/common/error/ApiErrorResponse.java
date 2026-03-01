package nuts.commerce.settlement.common.error;

import java.time.Instant;

public record ApiErrorResponse(
        ErrorCode code,
        String message,
        Instant timestamp,
        String path,
        String traceId
) {
    public static ApiErrorResponse of(ErrorCode code, String message, String path, String traceId) {
        return new ApiErrorResponse(code, message, Instant.now(), path, traceId);
    }
}