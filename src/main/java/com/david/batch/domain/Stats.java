package com.david.batch.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
public class Stats {

    private int errors;
    private int processed;
    private List<Product> writtenItems = new ArrayList<>();
    private Map<Category,Integer> categoryQuantities =  new HashMap<>();

    public Stats() {
        categoryQuantities.put(Category.COMPUTER, 0);
        categoryQuantities.put(Category.PHONE, 0);
    }

    public void addError(){
        errors++;
    }

    public void addProcessed(int added){
        processed += added;
    }

}
