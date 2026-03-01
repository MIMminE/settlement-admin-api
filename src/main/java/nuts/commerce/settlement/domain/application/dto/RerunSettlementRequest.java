package nuts.commerce.settlement.domain.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RerunSettlementRequest(
        @NotNull Long sellerId,
        @NotNull LocalDate settlementDate
) {
}