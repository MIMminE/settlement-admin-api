package nuts.commerce.settlement;

import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import nuts.commerce.settlement.domain.repository.SettlementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@SpringBatchTest
class SettlementBatchIntegrationTest {

    @Autowired
    JobOperator jobOperator;

    @Autowired
    Object jobExplorer;

    @Autowired
    Job dailySettlementJob;

    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    SettlementRepository settlementRepository;

    @Test
    void dailySettlementJob_createsSettlementForSeller() throws Exception {
        Seller s = sellerRepository.save(new Seller("sellerA", "123-45-67890"));

        // KST 기준 2026-03-01 10:00 결제
        ZoneId kst = ZoneId.of("Asia/Seoul");
        Instant paidAt = LocalDateTime.of(2026, 3, 1, 10, 0).atZone(kst).toInstant();
        orderRepository.save(new Order(s, new BigDecimal("10000.00"), paidAt));

        String params = "settlementDate=2026-03-01,run.id=" + System.currentTimeMillis();

        var startMethod = jobOperator.getClass().getMethod("start", String.class, String.class);
        Object execIdObj = startMethod.invoke(jobOperator, dailySettlementJob.getName(), params);

        Long execId;
        if (execIdObj instanceof Long) {
            execId = (Long) execIdObj;
        } else if (execIdObj instanceof Integer) {
            execId = ((Integer) execIdObj).longValue();
        } else if (execIdObj instanceof Number) {
            execId = ((Number) execIdObj).longValue();
        } else {
            execId = Long.parseLong(execIdObj.toString());
        }

        Method getJobExecution = jobExplorer.getClass().getMethod("getJobExecution", Long.class);
        Object execObj = getJobExecution.invoke(jobExplorer, execId);
        Method getStatus = execObj.getClass().getMethod("getStatus");
        Object statusObj = getStatus.invoke(execObj);

        assertThat(statusObj.toString()).isEqualTo(BatchStatus.COMPLETED.toString());

        var latest = settlementRepository.findTopBySellerIdAndSettlementDateOrderByVersionDesc(
                s.getId(), LocalDate.of(2026, 3, 1)
        );
        assertThat(latest).isPresent();
        assertThat(latest.get().getGrossAmount()).isEqualByComparingTo("10000.00");
    }
}