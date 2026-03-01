package nuts.commerce.settlement.domain.service;


import nuts.commerce.settlement.domain.model.AuditLog;
import nuts.commerce.settlement.domain.model.enums.AuditAction;
import nuts.commerce.settlement.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(AuditAction action, String actor, String message) {
        auditLogRepository.save(new AuditLog(action, actor, message));
    }
}