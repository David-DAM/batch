package com.david.batch.listener;

import com.david.batch.domain.Category;
import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Component
public class JobListener implements JobExecutionListener {

    private final Stats stats;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("""
                *************************************************
                *****************ECOMMERCE BATCH*****************
                *************************************************
                """);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        System.out.println("Errors: "+stats.getErrors());
        System.out.println("Total processed: "+stats.getProcessed());
        System.out.println("""
                *************************************************
                ***************END ECOMMERCE BATCH***************
                *************************************************
                """);
        log.info("IMPORT PRODUCTS BATCH ENDED CORRECTLY AT: {}", new Date().toString());

        stats.setErrors(0);
        stats.setProcessed(0);
        stats.getCategoryQuantities().put(Category.COMPUTER,0);
        stats.getCategoryQuantities().put(Category.PHONE,0);
        stats.getWrittenItems().clear();
    }
}
