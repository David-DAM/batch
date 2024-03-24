package com.david.batch.config;

import com.david.batch.domain.Product;
import com.david.batch.processor.ProductProcessor;
import com.david.batch.processor.SendEmailStats;
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
    private final SendEmailStats sendEmailStats;
    private final ProductProcessor processor;
    private final JobListener jobListener;
    private final ReaderListener readerListener;
    private final WritterListener writterListener;

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
    public RepositoryItemWriter<Product> writer(){
        RepositoryItemWriter<Product> writer =  new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        writer.setMethodName("save");

        return writer;
    }
    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor asyncTaskExecutor =  new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }
    @Bean
    public Step step1(){
        return new StepBuilder("csvImport", jobRepository)
                .<Product,Product>chunk(10, platformTransactionManager)
                .reader(reader())
                .processor(processor)
                .writer(writer())
                .listener(writterListener)
                .listener(readerListener)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob(){
        return new JobBuilder("importProducts",jobRepository)
                .start(step1())
                .listener(jobListener)
                .next(step2())
                .build();
    }

    @Bean
    public Step step2(){
        return new StepBuilder("sendEmailResults", jobRepository)
                .tasklet(sendEmailStats, platformTransactionManager)
                .build();
    }

}
