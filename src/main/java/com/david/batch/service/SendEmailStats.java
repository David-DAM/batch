package com.david.batch.service;

import com.david.batch.domain.Category;
import com.david.batch.domain.ProductDTO;
import com.david.batch.domain.ProductDTOMapper;
import com.david.batch.domain.Stats;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailStats implements Tasklet {

    private final Stats stats;
    private final JavaMailSender mailSender;
    private final ReportGenerator reportGenerator;
    private final ProductDTOMapper productDTOMapper;
    @Value("${spring.mail.personal.username}")
    private String personalEmail;
    @Value("${spring.mail.username}")
    private String senderEmail;
    private String text =
            """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Results of import process</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background-color: #f9f9f9;
                            }
                            h1 {
                                color: #333;
                                text-align: center;
                            }
                            table {
                                width: 80%;
                                margin: 20px auto;
                                border-collapse: collapse;
                                border: 1px solid #ddd;
                                background-color: #fff;
                            }
                            th, td {
                                padding: 10px;
                                text-align: left;
                            }
                            th {
                                background-color: #f2f2f2;
                            }
                            tr:nth-child(even) {
                                background-color: #f9f9f9;
                            }
                            tr:hover {
                                background-color: #e0e0e0;
                            }
                        </style>
                    </head>
                    <body>
                        <h1>Results of import process</h1>
                        <table>
                            <tr>
                                <th>ERRORS</th>
                                <th>PROCESSED</th>
                                <th>COMPUTERS PROCESSED</th>
                                <th>PHONES PROCESSED</th>
                            </tr>
                            <tr>
                                <td>{errors}</td>
                                <td>{processes}</td>
                                <td>{computers}</td>
                                <td>{phones}</td>
                            </tr>
                        </table>
                    </body>
                    </html>
            """;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        try {

            reportGenerator.exportToPdf(stats.getWrittenItems()
                    .stream()
                    .map(productDTOMapper)
                    .toList()
            );

            sendEmail();

            log.info("Import products report send correctly");

        } catch (MessagingException e){
            log.error("Error sending email with products report");
        } catch (Exception e){
            log.error("Unexpected error, import products report could not be send: " +e.getMessage());
        }

        return RepeatStatus.FINISHED;
    }

    private void sendEmail() throws MessagingException {
        text = text.replace("{errors}",String.valueOf(stats.getErrors()));
        text = text.replace("{processes}",String.valueOf(stats.getProcessed()));
        text = text.replace("{computers}",String.valueOf(stats.getCategoryQuantities().get(Category.COMPUTER)));
        text = text.replace("{phones}",String.valueOf(stats.getCategoryQuantities().get(Category.PHONE)));

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(senderEmail);
        helper.setTo(personalEmail);
        helper.setSubject("IMPORT PRODUCTS REPORT");
        helper.setText(text,text);


        FileSystemResource csv = new FileSystemResource(new File("src/main/resources/report.csv"));
        helper.addAttachment("report.csv", csv);

        FileSystemResource pdf = new FileSystemResource(new File("src/main/resources/report.pdf"));
        helper.addAttachment("report.pdf", pdf);

        mailSender.send(helper.getMimeMessage());
    }
}
