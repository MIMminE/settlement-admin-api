package nuts.commerce.settlement.domain.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class DailySettlementJobConfig {

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository, Step dailySettlementStep) {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .start(dailySettlementStep)
                .build();
    }

    @Bean
    public Step dailySettlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DailySettlementTasklet tasklet
    ) {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}