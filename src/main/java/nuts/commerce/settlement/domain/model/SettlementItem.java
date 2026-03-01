package nuts.commerce.settlement.domain.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "settlement_item",
        indexes = {
                @Index(name = "idx_settlement_item_settlement", columnList = "settlement_id")
        })
@Getter
public class SettlementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Settlement settlement;

    @Column(nullable = false)
    private String type;

    private Long orderId;
    private Long refundId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    protected SettlementItem() {
    }

    public static SettlementItem order(Settlement settlement, Long orderId, BigDecimal amount) {
        SettlementItem item = new SettlementItem();
        item.settlement = settlement;
        item.type = "ORDER";
        item.orderId = orderId;
        item.amount = amount;
        return item;
    }

    public static SettlementItem refund(Settlement settlement, Long refundId, Long orderId, BigDecimal amount) {
        SettlementItem item = new SettlementItem();
        item.settlement = settlement;
        item.type = "REFUND";
        item.refundId = refundId;
        item.orderId = orderId;
        item.amount = amount; // 보통 음수
        return item;
    }
}