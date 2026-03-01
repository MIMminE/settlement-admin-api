package nuts.commerce.settlement.domain.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateRefundRequest(
        @NotNull Long sellerId,
        @NotNull Long orderId,
        @NotNull BigDecimal refundedAmount,
        @NotNull Instant refundedAt
) {
}