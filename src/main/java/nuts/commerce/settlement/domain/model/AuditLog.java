package nuts.commerce.settlement.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import nuts.commerce.settlement.domain.model.enums.AuditAction;

import java.time.Instant;

@Entity
@Table(name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_createdat", columnList = "createdAt")
        })
@Getter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(nullable = false, length = 80)
    private String actor;

    @Column(length = 200)
    private String message;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {
    }

    public AuditLog(AuditAction action, String actor, String message) {
        this.action = action;
        this.actor = actor;
        this.message = message;
    }

}