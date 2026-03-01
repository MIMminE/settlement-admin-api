package nuts.commerce.settlement.domain.application.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmSettlementRequest(@NotNull Long settlementId) {
}