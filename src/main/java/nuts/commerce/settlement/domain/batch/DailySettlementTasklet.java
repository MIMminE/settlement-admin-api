package nuts.commerce.settlement.domain.batch;

import nuts.commerce.settlement.domain.model.enums.AuditAction;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import nuts.commerce.settlement.domain.repository.SettlementRepository;
import nuts.commerce.settlement.domain.service.AuditService;
import nuts.commerce.settlement.domain.service.SettlementCommandService;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailySettlementTasklet implements Tasklet {

    private final SellerRepository sellerRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementCommandService settlementCommandService;
    private final AuditService auditService;

    public DailySettlementTasklet(
            SellerRepository sellerRepository,
            SettlementRepository settlementRepository,
            SettlementCommandService settlementCommandService,
            AuditService auditService
    ) {
        this.sellerRepository = sellerRepository;
        this.settlementRepository = settlementRepository;
        this.settlementCommandService = settlementCommandService;
        this.auditService = auditService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String dateStr = (String) chunkContext.getStepContext().getJobParameters().get("settlementDate");
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException("JobParameter 'settlementDate' is required. e.g. 2026-03-01");
        }
        LocalDate date = LocalDate.parse(dateStr);

        sellerRepository.findAll().forEach(seller -> {
            try {
                boolean exists = settlementRepository
                        .findTopBySellerIdAndSettlementDateOrderByVersionDesc(seller.getId(), date)
                        .isPresent();

                if (!exists) {
                    settlementCommandService.calculateDaily(seller.getId(), date, 1, "batch");
                }
            } catch (Exception e) {
                auditService.record(
                        AuditAction.SETTLEMENT_CALCULATED,
                        "batch",
                        "FAILED sellerId=" + seller.getId() + " date=" + date + " err=" + e.getMessage()
                );
                // 스킵
            }
        });

        return RepeatStatus.FINISHED;
    }
}