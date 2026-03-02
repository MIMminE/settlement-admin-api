package nuts.commerce.settlement.domain.batch;

import nuts.commerce.settlement.domain.model.enums.AuditAction;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import nuts.commerce.settlement.domain.repository.SettlementRepository;
import nuts.commerce.settlement.domain.service.AuditService;
import nuts.commerce.settlement.domain.service.SettlementCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailySettlementTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DailySettlementTasklet.class);

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
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        String dateStr = (String) chunkContext.getStepContext().getJobParameters().get("settlementDate");
        LocalDate date;
        if (dateStr == null || dateStr.isBlank()) {
            // 기본값: 어제 날짜
            date = LocalDate.now().minusDays(1);
            log.warn("JobParameter 'settlementDate' not provided. Using default date={}", date);
        } else {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid 'settlementDate' parameter. Expected format: yyyy-MM-dd", e);
            }
        }

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