package com.david.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        //ElasticApmAttacher.attach();
        SpringApplication.run(BatchApplication.class, args);
    }

}
