package com.david.batch.config;

import com.david.batch.domain.Product;
import com.david.batch.domain.ProductResult;
import com.david.batch.domain.Stats;
import com.david.batch.processor.ProductProcessor;
import com.david.batch.processor.ResultProcessor;
import com.david.batch.processor.SendEmailStats;
import com.david.batch.repository.ProductRepository;
import com.david.batch.listener.JobListener;
import com.david.batch.listener.ReaderListener;
import com.david.batch.listener.WritterListener;
import com.david.batch.writer.DatabaseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final ProductRepository productRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SendEmailStats sendEmailStats;
    private final ProductProcessor processorStep1;
    private final ResultProcessor processorStep2;
    private final JobListener jobListener;
    private final ReaderListener readerListenerStep1;
    private final WritterListener writerListenerStep1;
    private final DatabaseWriter writerStep1;
    private final Stats stats;

    @Bean
    public FlatFileItemReader<Product> readerStep1(){
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
    public RepositoryItemWriter<Product> writerStep1(){
        RepositoryItemWriter<Product> writer =  new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        writer.setMethodName("save");

        return writer;
    }
    @Bean
    public FlatFileItemWriter<ProductResult> writerStep2(){
        FlatFileItemWriter<ProductResult> writer = new FlatFileItemWriter<>();

        FileSystemResource resource = new FileSystemResource("src/main/resources/report.csv");
        writer.setEncoding("UTF-8");
        writer.setResource(resource);
        writer.setShouldDeleteIfExists(true);

        BeanWrapperFieldExtractor<ProductResult> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] { "id", "name", "category" });

        DelimitedLineAggregator<ProductResult> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);

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
        return new StepBuilder("csvImportDatabase", jobRepository)
                .<Product,Product>chunk(10, platformTransactionManager)
                .reader(readerStep1())
                .processor(processorStep1)
                .writer(writerStep1)
                .listener(writerListenerStep1)
                .listener(readerListenerStep1)
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean
    @StepScope
    public ItemReader<Product> readerStep2(){

        List<Product> processedItems = stats.getProcessedItems().stream().toList();
        stats.getProcessedItems().clear();
        return new ListItemReader<Product>(processedItems);
    }

    @Bean
    public Step step2(){
        return new StepBuilder("csvFileResultsImport", jobRepository)
                .<Product,ProductResult>chunk(10, platformTransactionManager)
                .reader(readerStep2())
                .processor(processorStep2)
                .writer(writerStep2())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob(){
        return new JobBuilder("importProducts",jobRepository)
                .start(step1())
                .listener(jobListener)
                .next(step2())
                .next(step3())
                .build();
    }

    @Bean
    public Step step3(){
        return new StepBuilder("sendEmailResults", jobRepository)
                .tasklet(sendEmailStats, platformTransactionManager)
                .build();
    }

}
