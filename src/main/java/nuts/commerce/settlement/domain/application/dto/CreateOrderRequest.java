package nuts.commerce.settlement.domain.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateOrderRequest(
        @NotNull Long sellerId,
        @NotNull BigDecimal paidAmount,
        @NotNull Instant paidAt
) {
}