package nuts.commerce.settlement.domain.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/batch")
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);

    private final JobOperator jobOperator;
    private final Job dailySettlementJob;

    public BatchController(JobOperator jobOperator, Job dailySettlementJob) {
        this.jobOperator = jobOperator;
        this.dailySettlementJob = dailySettlementJob;
    }

    public record JobStartResponse(Long executionId, String jobName, LocalDate settlementDate, String status) {
    }

    @PostMapping("/daily-settlement")
    public ResponseEntity<JobStartResponse> runDailySettlement(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate settlementDate
    ) throws Exception {

        LocalDate date = (settlementDate != null) ? settlementDate : LocalDate.now().minusDays(1);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("settlementDate", date.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("Starting job={} params={}", dailySettlementJob.getName(), jobParameters);

        JobExecution execution = jobOperator.start(dailySettlementJob, jobParameters);

        return ResponseEntity.accepted().body(new JobStartResponse(
                execution.getId(),
                dailySettlementJob.getName(),
                date,
                execution.getStatus().name()
        ));
    }
}