package nuts.commerce.settlement.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import nuts.commerce.settlement.domain.model.enums.SettlementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "settlement",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_settlement_seller_date_version", columnNames = {"seller_id", "settlementDate", "version"})
        },
        indexes = {
                @Index(name = "idx_settlement_date_status", columnList = "settlementDate, status")
        }
)
@Getter
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Seller seller;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementStatus status = SettlementStatus.CREATED;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    private Instant calculatedAt;
    private Instant confirmedAt;

    @Column(length = 80)
    private String confirmedBy;

    protected Settlement() {
    }

    public Settlement(Seller seller, LocalDate settlementDate, int version) {
        this.seller = seller;
        this.settlementDate = settlementDate;
        this.version = version;
    }

    public void markCalculated(BigDecimal gross, BigDecimal refund, BigDecimal fee, BigDecimal net) {
        this.grossAmount = gross;
        this.refundAmount = refund;
        this.feeAmount = fee;
        this.netAmount = net;
        this.status = SettlementStatus.CALCULATED;
        this.calculatedAt = Instant.now();
    }

    public void confirm(String adminUsername) {
        if (this.status != SettlementStatus.CALCULATED) {
            throw new IllegalStateException("Only CALCULATED settlement can be CONFIRMED.");
        }
        this.status = SettlementStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
        this.confirmedBy = adminUsername;
    }

    public void supersede() {
        this.status = SettlementStatus.SUPERSEDED;
    }
}