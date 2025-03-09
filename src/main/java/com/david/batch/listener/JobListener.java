package com.david.batch.listener;

import com.david.batch.domain.Stats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info("Errors: {}", jobExecution.getExecutionContext().getInt(Stats.TOTAL_ERRORS.name(), 0));
        log.info("Total processed: {}", jobExecution.getExecutionContext().getInt(Stats.TOTAL_PROCESSED.name(), 0));
        log.info("IMPORT PRODUCTS BATCH ENDED CORRECTLY AT: {}", LocalDateTime.now());
    }
}
