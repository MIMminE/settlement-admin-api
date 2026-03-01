package nuts.commerce.settlement.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import nuts.commerce.settlement.domain.model.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "refund",
        indexes = {
                @Index(name = "idx_refund_seller_refundedat", columnList = "seller_id, refundedAt")
        })
@Getter
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status = RefundStatus.REFUNDED;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    @Column(nullable = false)
    private Instant refundedAt;

    protected Refund() {
    }

    public Refund(Seller seller, Order order, BigDecimal refundedAmount, Instant refundedAt) {
        this.seller = seller;
        this.order = order;
        this.refundedAmount = refundedAmount;
        this.refundedAt = refundedAt;
    }
}