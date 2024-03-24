package com.david.batch.listener;

import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JobListener implements JobExecutionListener {

    private final Stats stats;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("*****************ECOMMERCE BATCH*****************");
        System.out.println("*************************************************");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Errors: "+stats.getErrors());
        System.out.println("Well processed: "+stats.getWellProcessed());
        System.out.println("Total processed: "+stats.getProcessed());
        System.out.println("*************************************************");
        System.out.println("*************************************************");
    }
}
