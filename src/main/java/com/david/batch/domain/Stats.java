package com.david.batch.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class Stats {

    private int errors;
    private int wellProcessed;
    private int processed;
    private List<Product> processedItems = new ArrayList<>();

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
