package nuts.commerce.settlement.domain.model;


import jakarta.persistence.*;
import lombok.Getter;
import nuts.commerce.settlement.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_seller_paidat", columnList = "seller_id, paidAt")
        })
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PAID;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private Instant paidAt;

    protected Order() {
    }

    public Order(Seller seller, BigDecimal paidAmount, Instant paidAt) {
        this.seller = seller;
        this.paidAmount = paidAmount;
        this.paidAt = paidAt;
    }
}