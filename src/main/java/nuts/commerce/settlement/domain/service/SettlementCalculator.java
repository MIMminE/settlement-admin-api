package nuts.commerce.settlement.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SettlementCalculator {

    private final BigDecimal feeRate; // e.g. 0.05

    public SettlementCalculator(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public BigDecimal feeOf(BigDecimal grossMinusRefund) {
        if (grossMinusRefund.signum() <= 0) return BigDecimal.ZERO;
        return grossMinusRefund.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
    }
}