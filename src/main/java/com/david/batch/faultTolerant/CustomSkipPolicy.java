package com.david.batch.faultTolerant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

@Component
@Slf4j
public class CustomSkipPolicy implements SkipPolicy {

    private final Integer SKIPLIMIT = 1;

    @Override
    public boolean shouldSkip(Throwable exception, long skipCount) throws SkipLimitExceededException {

        if (exception instanceof FileNotFoundException) {
            return false;
        } else if (exception instanceof FlatFileParseException && skipCount < SKIPLIMIT) {

            FlatFileParseException flatFileParseException = (FlatFileParseException) exception;
            String input = flatFileParseException.getInput();
            int lineNumber = flatFileParseException.getLineNumber();

            log.error("Error parsing file with input {}, at line {}:",input,lineNumber);

            return true;
        }

        return true;
    }
}
