package nuts.commerce.settlement.domain.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name = "seller")
@Getter
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40)
    private String businessNo;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Seller() {
    }

    public Seller(String name, String businessNo) {
        this.name = name;
        this.businessNo = businessNo;
    }
}