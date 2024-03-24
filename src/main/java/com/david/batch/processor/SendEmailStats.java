package com.david.batch.processor;

import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

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
    private String text = "Errors: {errors}\nWell processed: {wellProcessed}\nTotal processed: {processed}";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(personalEmail);
            message.setFrom(senderEmail);
            message.setSubject("IMPORT PRODUCTS REPORT");

            text = text.replace("{errors}",String.valueOf(stats.getErrors()));
            text = text.replace("{wellProcessed}",String.valueOf(stats.getWellProcessed()));
            text = text.replace("{processed}",String.valueOf(stats.getProcessed()));

            message.setText(text);

            mailSender.send(message);

            log.info("Import products report send correctly");

        }catch (MailException e){
            log.error("Import products report could not be send");
        }


        return null;
    }
}
