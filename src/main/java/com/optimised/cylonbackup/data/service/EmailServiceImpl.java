package com.optimised.cylonbackup.data.service;

import com.vaadin.flow.component.notification.Notification;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by Olga on 7/15/2016.
 */

@Log4j2
@Service("EmailService")
public class EmailServiceImpl implements EmailService {
    private static final String NO_REPLY_ADDRESS = "noreply@optimised.net";
    final static Marker DB = MarkerManager.getMarker("DB");
    final private JavaMailSender emailSender;
    final private EmailSettingService emailSettingService;

    public EmailServiceImpl(JavaMailSender emailSender, EmailSettingService emailSettingService) {
        this.emailSender = emailSender;
        this.emailSettingService = emailSettingService;
    }

    public boolean sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(NO_REPLY_ADDRESS);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
        } catch (MailException e) {
            log.error("{}{}", "Failed to send email ", e.getMessage());
            return false;
        }
        return true;
    }


    @Override
    public boolean sendMessageWithAttachment(String to,
                                             String subject,
                                             String text,
                                             String pathToAttachment) {
        try {

            MimeMessage message = emailSender.createMimeMessage();
            // pass 'true' to the constructor to create a multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) emailSender;


            Properties mailProperties = mailSender.getJavaMailProperties();
 //           mailProperties.put("mail.transport.protocol", "smtp");
            mailProperties.put("mail.smtp.auth", "true");
            mailProperties.put("mail.smtp.starttls.enable", "true");
//            mailProperties.put("mail.smtp.host", "smtp.gmail.com");
//            mailProperties.put("mail.smtp.port", "587");
            mailProperties.put("mail.smtp.starttls.required", "true");
            mailSender.setJavaMailProperties(mailProperties);

            mailSender.setUsername(emailSettingService.getSetting().getUserName());
            mailSender.setPassword(emailSettingService.getSetting().getUserPassword());
            mailSender.setProtocol("smtp");
            mailSender.setHost(emailSettingService.getSetting().getSmtpHost());
            mailSender.setPort(emailSettingService.getSetting().getSmtpPort());



            helper.setFrom(emailSettingService.getSetting().getSmtpFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
//            ((JavaMailSenderImpl) emailSender).setUsername(emailSettingService.getSetting().getUserName());
//            ((JavaMailSenderImpl) emailSender).setPassword(emailSettingService.getSetting().getUserPassword());
//            ((JavaMailSenderImpl) emailSender).setHost(emailSettingService.getSetting().getHost());
//            ((JavaMailSenderImpl) emailSender).setPort(emailSettingService.getSetting().getPort());
          //  ((JavaMailSenderImpl) emailSender).setJavaMailProperties(mailProperties);
            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);

            emailSender.send(message);
        } catch (jakarta.mail.MessagingException | MailSendException | MailAuthenticationException e) {
            Notification.show("Failed to send email " + e.getMessage());
            log.error(DB,"{}{}", "Failed to send email ", e.getMessage());
            return false;
        }
        return true;
    }
}