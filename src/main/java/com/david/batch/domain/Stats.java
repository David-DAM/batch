package com.david.batch.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Stats {

    private int errors;
    private int wellProcessed;
    private int processed;

    public void addError(){
        errors++;
    }

    public void addWellProcessed(){
        wellProcessed++;
    }

    public void addProcessed(){
        processed++;
    }

}
