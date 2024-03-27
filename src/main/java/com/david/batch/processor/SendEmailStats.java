package com.david.batch.processor;

import com.david.batch.domain.Stats;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailStats implements Tasklet {

    private final Stats stats;
    private final JavaMailSender mailSender;
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
                            </tr>
                            <tr>
                                <td>{errors}</td>
                                <td>{processed}</td>
                            </tr>
                        </table>
                    </body>
                    </html>
            """;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        try {

            text = text.replace("{errors}",String.valueOf(stats.getErrors()));
            text = text.replace("{wellProcessed}",String.valueOf(stats.getWellProcessed()));
            text = text.replace("{processed}",String.valueOf(stats.getProcessed()));

            //TODO set stats to 0

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(personalEmail);
            helper.setSubject("IMPORT PRODUCTS REPORT");
            helper.setText(text,text);


            FileSystemResource file = new FileSystemResource(new File("src/main/resources/report.csv"));
            helper.addAttachment("report.csv", file);

            mailSender.send(helper.getMimeMessage());

            log.info("Import products report send correctly");

        }catch (MailException e){
            log.error("Import products report could not be send");
        }


        return null;
    }
}
