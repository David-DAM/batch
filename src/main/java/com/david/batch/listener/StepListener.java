package com.david.batch.listener;

import com.david.batch.domain.Stats;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class StepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        //StepExecutionListener.super.beforeStep(stepExecution);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();

        long stepWrittenCount = stepExecution.getWriteCount();
        long stepSkipCount = stepExecution.getSkipCount();

        int totalProcessed = executionContext.getInt(Stats.TOTAL_PROCESSED.name(), 0);
        int totalErrors = executionContext.getInt(Stats.TOTAL_ERRORS.name(), 0);

        executionContext.putInt(Stats.TOTAL_PROCESSED.name(), totalProcessed + (int) stepWrittenCount);
        executionContext.putInt(Stats.TOTAL_ERRORS.name(), totalErrors + (int) stepSkipCount);

        return ExitStatus.COMPLETED;
    }
}
