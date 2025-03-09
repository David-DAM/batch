package com.david.batch.service;

import com.david.batch.domain.Category;
import com.david.batch.domain.Product;
import com.david.batch.domain.ProductDTOMapper;
import com.david.batch.domain.Stats;
import com.david.batch.repository.ProductRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailStats implements Tasklet {

    public static final String REPORT_CSV_PATH = "src/main/resources/report.csv";
    public static final String REPORT_PDF_PATH = "src/main/resources/report.pdf";
    public static final String REPORT_CSV = "report.csv";
    public static final String REPORT_PDF = "report.pdf";
    public static final String EMAIL_REPORT_HTML = "classpath:/templates/emailReport.html";
    public static final String EMAIL_SUBJECT = "IMPORT PRODUCTS REPORT";
    private final JavaMailSender mailSender;
    private final ReportGenerator reportGenerator;
    private final ProductDTOMapper productDTOMapper;
    private final ProductRepository productRepository;
    private final ResourceLoader resourceLoader;
    @Value("${spring.mail.to}")
    private String toEmail;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        int totalErrors = contribution.getStepExecution().getJobExecution().getExecutionContext().getInt(Stats.TOTAL_ERRORS.name(), 0);
        int totalProcessed = contribution.getStepExecution().getJobExecution().getExecutionContext().getInt(Stats.TOTAL_PROCESSED.name(), 0);

        List<Product> products = productRepository.findAll();

        try {

            reportGenerator.exportToPdf(products
                    .stream()
                    .map(productDTOMapper)
                    .toList()
            );

            sendEmail(products, totalErrors, totalProcessed);

            log.info("Import products report send correctly");

        } catch (MessagingException e) {
            log.error("Error sending email with products report");
        } catch (Exception e) {
            log.error("Unexpected error, import products report could not be send: {}", e.getMessage());
        }

        return RepeatStatus.FINISHED;
    }

    private void sendEmail(List<Product> products, int totalErrors, int totalProcessed) throws MessagingException, IOException {
        Resource resource = resourceLoader.getResource(EMAIL_REPORT_HTML);
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        content = content.replace("{errors}", String.valueOf(totalErrors));
        content = content.replace("{processes}", String.valueOf(totalProcessed));
        content = content.replace("{computers}", String.valueOf(products.stream().filter(product -> product.getCategory().equals(Category.COMPUTER)).count()));
        content = content.replace("{phones}", String.valueOf(products.stream().filter(product -> product.getCategory().equals(Category.PHONE)).count()));

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(EMAIL_SUBJECT);
        helper.setText(content, content);


        FileSystemResource csv = new FileSystemResource(new File(REPORT_CSV_PATH));
        helper.addAttachment(REPORT_CSV, csv);

        FileSystemResource pdf = new FileSystemResource(new File(REPORT_PDF_PATH));
        helper.addAttachment(REPORT_PDF, pdf);

        mailSender.send(helper.getMimeMessage());
    }
}
