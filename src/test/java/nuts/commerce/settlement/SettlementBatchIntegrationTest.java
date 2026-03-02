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
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@SpringBatchTest
@Testcontainers
class SettlementBatchIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("settlement")
            .withUsername("settlement")
            .withPassword("settlement");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.batch.jdbc.initialize-schema", () -> "always");
        r.add("spring.test.database.replace", () -> "NONE");
    }

    @Autowired
    JobOperator jobOperator;

    @Autowired
    ApplicationContext applicationContext;

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
        // Fix JVM default timezone in test to avoid environment-dependent behavior
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

        Seller s = sellerRepository.save(new Seller("sellerA", "123-45-67890"));

        // KST 기준 2026-03-01 10:00 결제
        ZoneId kst = ZoneId.of("Asia/Seoul");
        Instant paidAt = LocalDateTime.of(2026, 3, 1, 10, 0).atZone(kst).toInstant();
        orderRepository.save(new Order(s, new BigDecimal("10000.00"), paidAt));

        String params = "settlementDate=2026-03-01,run.id=" + System.currentTimeMillis();

        // Use reflection to invoke start to avoid compile-time signature issues across Spring Batch versions
        var startMethod = jobOperator.getClass().getMethod("start", String.class, String.class);
        Object execIdObj = startMethod.invoke(jobOperator, dailySettlementJob.getName(), params);

        // Normalize execution id to Long
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

        // Get jobExplorer bean from ApplicationContext and use reflection to get JobExecution and its status
        Object jobExplorer = applicationContext.getBean("jobExplorer");
        Method getJobExecution = jobExplorer.getClass().getMethod("getJobExecution", Long.class);
        Object execObj = getJobExecution.invoke(jobExplorer, execId);
        Method getStatus = execObj.getClass().getMethod("getStatus");
        Object statusObj = getStatus.invoke(execObj);

        assertThat(statusObj.toString()).isEqualTo(BatchStatus.COMPLETED.toString());

        var latestOpt = settlementRepository.findTopBySellerIdAndSettlementDateOrderByVersionDesc(
                s.getId(), LocalDate.of(2026, 3, 1)
        );
        var latest = latestOpt.orElseThrow();
        assertThat(latest.getGrossAmount()).isEqualByComparingTo("10000.00");
    }
}