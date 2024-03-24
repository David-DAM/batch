package com.david.batch.config;

import com.david.batch.domain.Product;
import com.david.batch.processor.ProductProcessor;
import com.david.batch.repository.ProductRepository;
import com.david.batch.domain.Stats;
import com.david.batch.listener.JobListener;
import com.david.batch.listener.ReaderListener;
import com.david.batch.listener.WritterListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final ProductRepository productRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final Stats stats;

    @Bean
    public FlatFileItemReader<Product> reader(){
        FlatFileItemReader<Product> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/products.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());

        return itemReader;
    }
    @Bean
    public LineMapper<Product> lineMapper(){
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","name","description","price","image","category");

        BeanWrapperFieldSetMapper<Product> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Product.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
    @Bean
    public ProductProcessor processor(){
        return new ProductProcessor(stats);
    }
    @Bean
    public RepositoryItemWriter<Product> writer(){
        RepositoryItemWriter<Product> writer =  new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        writer.setMethodName("save");

        return writer;
    }
    @Bean
    public WritterListener writterListener(){
        return new WritterListener(stats);
    }
    public ReaderListener readerListener(){
        return new ReaderListener(stats);
    }
    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor asyncTaskExecutor =  new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }
    @Bean
    public Step importStep(){
        return new StepBuilder("csvImport", jobRepository)
                .<Product,Product>chunk(10, platformTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .listener(writterListener())
                .listener(readerListener())
                .taskExecutor(taskExecutor())
                .build();
    }
    @Bean
    public JobListener jobListener(){
        return new JobListener(stats);
    }

    @Bean
    public Job runJob(){
        return new JobBuilder("importProducts",jobRepository)
                .start(importStep())
                .listener(jobListener())
                .build();
    }

}
