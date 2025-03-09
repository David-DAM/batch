package com.david.batch.config;

import com.david.batch.decision.FlowDecision;
import com.david.batch.domain.Product;
import com.david.batch.domain.ProductDTO;
import com.david.batch.faultTolerant.CustomSkipPolicy;
import com.david.batch.listener.*;
import com.david.batch.processor.ProductDTOProcessor;
import com.david.batch.processor.ProductProcessor;
import com.david.batch.repository.ProductRepository;
import com.david.batch.service.SendEmailStats;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    public static final String PRODUCTS_CSV_PATH = "src/main/resources/products.csv";
    private final ProductRepository productRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SendEmailStats sendEmailStats;
    private final ProductProcessor processorStep1;
    private final ProductDTOProcessor processorStep2;
    private final ReaderListener readerListenerStep1;
    private final ProcessorListener processorListenerStep2;
    private final WriterListener writerListenerStep1;
    private final JobListener jobListener;
    private final CustomSkipPolicy customSkipPolicy;
    private final FlowDecision flowDecision;
    private final StepListener stepListener;

    @Bean
    public FlatFileItemReader<Product> readerStep1() {
        FlatFileItemReader<Product> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(PRODUCTS_CSV_PATH));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());

        return itemReader;
    }

    @Bean
    public LineMapper<Product> lineMapper() {
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(true);
        lineTokenizer.setNames("id", "name", "description", "price", "image", "category");

        BeanWrapperFieldSetMapper<Product> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Product.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public RepositoryItemWriter<Product> writerStep1() {
        RepositoryItemWriter<Product> writer = new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        writer.setMethodName("save");

        return writer;
    }

    //Avoid task executor for problems with concurrency on Stats resources
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

    @Bean
    public Step step1() {
        return new StepBuilder("csvImportDatabase", jobRepository)
                .<Product, Product>chunk(10, platformTransactionManager)
                .reader(readerStep1())
                .listener(readerListenerStep1)
                .processor(processorStep1)
                .writer(writerStep1())
                .listener(writerListenerStep1)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)
                //.skipLimit(1)
                //.skip(FlatFileParseException.class)
                //.taskExecutor(taskExecutor())
                .listener(stepListener)
                .build();
    }

    @Bean
    public RepositoryItemReader<Product> readerStep2() {
        RepositoryItemReader<Product> reader = new RepositoryItemReader<>();
        reader.setRepository(productRepository);
        reader.setMethodName("findAll");
        reader.setSort(Map.of("id", Sort.Direction.ASC));

        return reader;
    }

    @Bean
    public FlatFileItemWriter<ProductDTO> writerStep2() {
        FlatFileItemWriter<ProductDTO> writer = new FlatFileItemWriter<>();

        FileSystemResource resource = new FileSystemResource(SendEmailStats.REPORT_CSV_PATH);
        writer.setEncoding("UTF-8");
        writer.setResource(resource);
        writer.setShouldDeleteIfExists(true);

        BeanWrapperFieldExtractor<ProductDTO> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "name", "category"});

        DelimitedLineAggregator<ProductDTO> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);

        return writer;
    }

    @Bean
    public Step step2() {
        return new StepBuilder("csvFileResultsImport", jobRepository)
                .<Product, ProductDTO>chunk(10, platformTransactionManager)
                .reader(readerStep2())
                .processor(processorStep2)
                .listener(processorListenerStep2)
                .writer(writerStep2())
                //.taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("sendEmailResults", jobRepository)
                .tasklet(sendEmailStats, platformTransactionManager)
                .build();
    }

    @Bean
    public Job runJob() {
        return new JobBuilder("importProducts", jobRepository)
                .start(step1())
                .next(flowDecision)
                //.on(FlowExecutionStatus.COMPLETED.toString()).end()
                .on(FlowExecutionStatus.COMPLETED.toString()).to(step2())
                .on(FlowExecutionStatus.FAILED.toString()).end()
                .from(step2())
                .next(step3())
                .end()
                .listener(jobListener)
                .build();
    }

}
