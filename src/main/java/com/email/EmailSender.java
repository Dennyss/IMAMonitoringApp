package com.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
@Component
public class EmailSender implements InitializingBean {
    private static final Logger logger = LogManager.getLogger(EmailSender.class);
    private Session session;

    @Value("${email.from.address}")
    private String fromAddress;

    @Value("${email.to.recipientsList}")
    private String toRecipientsList;

    @Value("${email.cc.recipientsList}")
    private String ccRecipientsList;

    @Value("${alert.threshold}")
    private int alertThreshold;

    @Value("${alert.to.recipientsList}")
    private String alertToRecipientsList;

    @Value("${alert.cc.recipientsList}")
    private String alertCcRecipientsList;

    @Override
    public void afterPropertiesSet() throws Exception {
        Properties props = new Properties();
        props.put("mail.host", "192.168.195.26");
        props.put("mail.port", "25");
        session = Session.getDefaultInstance(props);
    }

    public void sendNotification(int activeConnections) {
        if(activeConnections == 0){
            logger.warn("The number of connections is zero. Not enough time to collect data. Don't send an email");
            return;
        }

        String body = "Hello All,\n\n" +
                "Latest monitoring statistics for $SYS/ResourceStatistics/Endpoint:\n\n" +
                "Active connections: " + activeConnections +
                "\n\n\nThis email was sent by IMA Monitoring Application. Please do not reply on this message.";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toRecipientsList));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccRecipientsList));

            message.setSubject("IMA active connections: " + activeConnections);
            message.setText(body);

            Transport.send(message);
            logger.info("The email has been sent, connections number: " + activeConnections);
        } catch (MessagingException e) {
            logger.error("Error during sending email notification: ", e);
        }
    }

    public void sendAlertNotification(int activeConnections) {
        if(activeConnections == 0){
            logger.warn("The number of connections is zero. Not enough time to collect data. Don't send an alert email");
            return;
        }

        String body = "Hello All,\n\n" +
                "Active connections number significant drop detected: > " + alertThreshold + "% !" +
                "\nCurrent active connections number: " + activeConnections +
                "\n\n\nThis alert was sent by IMA Monitoring Application. Please do not reply on this message.";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(alertToRecipientsList));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(alertCcRecipientsList));

            message.setSubject("IMA Alert! Connections drop " + alertThreshold + "%! Currently: " + activeConnections);
            message.setText(body);

            Transport.send(message);
            logger.info("The alert has been sent, connections number: " + activeConnections + ". Threshold: " + alertThreshold);
        } catch (MessagingException e) {
            logger.error("Error during sending email alert: ", e);
        }
    }
}
