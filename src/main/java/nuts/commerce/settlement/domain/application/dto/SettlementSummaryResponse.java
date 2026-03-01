package nuts.commerce.settlement.domain.application.dto;


import nuts.commerce.settlement.domain.model.enums.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementSummaryResponse(
        Long settlementId,
        Long sellerId,
        String sellerName,
        LocalDate settlementDate,
        int version,
        SettlementStatus status,
        BigDecimal grossAmount,
        BigDecimal refundAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount
) {}