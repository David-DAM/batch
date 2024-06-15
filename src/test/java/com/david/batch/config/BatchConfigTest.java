package com.david.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringJUnitConfig(BatchConfig.class)
@Slf4j
@RequiredArgsConstructor
class BatchConfigTest {

    private static final Path INPUT_DIRECTORY = Path.of("target/info");
    private static final Path EXPECTED_COMPLETED_DIRECTORY = Path.of("target/info/completed");
    private static final Path EXPECTED_FAILED_DIRECTORY = Path.of("target/info/failed");

    private final JobLauncherTestUtils jobLauncherTestUtils;

    private final JobRepositoryTestUtils jobRepositoryTestUtils;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        if (Files.notExists(INPUT_DIRECTORY)){
            Files.createDirectories(INPUT_DIRECTORY);
        }
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        try (Stream<Path> fileTree = Files.walk(INPUT_DIRECTORY)) {
            fileTree.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .peek(x -> log.info("Deleting the file {}",x.getName()))
                    .forEach(File::delete);
        }

    }
    @SneakyThrows
    @Test
    public void readFromFileAndInsertIntoDatabaseAndMoveToCompletedDirectory(){
        Path fileTestToCompletedResult = Path.of(INPUT_DIRECTORY + File.separator + "products-completed.csv");
        Path inputFile = Files.createFile(fileTestToCompletedResult);

        Files.writeString(inputFile,BatchConfigTestDataProviderUtils.supplyValidContent());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("input.file.name", inputFile.toString())
                .addDate("uniqueness", new Date())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertEquals(ExitStatus.COMPLETED,jobExecution.getExitStatus());
    }
}