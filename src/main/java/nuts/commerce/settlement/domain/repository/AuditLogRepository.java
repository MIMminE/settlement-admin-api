package nuts.commerce.settlement.domain.repository;

import nuts.commerce.settlement.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}