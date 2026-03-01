package nuts.commerce.settlement.domain.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/batch")
public class BatchController {

    private final JobOperator jobOperator;
    private final Job dailySettlementJob;

    public BatchController(JobOperator jobOperator, Job dailySettlementJob) {
        this.jobOperator = jobOperator;
        this.dailySettlementJob = dailySettlementJob;
    }

    @PostMapping("/daily-settlement")
    public String run(@RequestParam LocalDate settlementDate) throws Exception {
        String params = "settlementDate=" + settlementDate.toString() + ",run.id=" + System.currentTimeMillis();

        Method startMethod = jobOperator.getClass().getMethod("start", String.class, String.class);
        Object execId = startMethod.invoke(jobOperator, dailySettlementJob.getName(), params);
        return "JOB STARTED id=" + execId;
    }
}