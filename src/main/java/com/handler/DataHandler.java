package com.handler;

import com.bean.DataStorage;
import com.email.EmailSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
@Component
public class DataHandler {
    private static final Logger logger = LogManager.getLogger(DataHandler.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private EmailSender emailSender;

    public void onMessage(String jsonPayload, String destination) {
        if (jsonPayload == null || jsonPayload.isEmpty()) {
            logger.warn("Incoming JSON payload is null or empty. Skipping it ...");
            return;
        }

        String topicEnd = getTopicEnd(destination);
        if ("Endpoint".equals(topicEnd)) {
            logger.debug("Received payload:\n" + jsonPayload + "\nFrom destination: " + destination);
            parsePayload(jsonPayload);
        }
    }

    private void parsePayload(String jsonPayload) {
        Map<String, Object> parsedPayload = null;

        try {
            parsedPayload = mapper.readValue(jsonPayload, HashMap.class);
        } catch (IOException e) {
            logger.error("Exception while parsing input payload. Stopping flow", e);
            return;
        }

        String endpointName = (String) parsedPayload.get("Name");
        int activeConnections = (Integer) parsedPayload.get("ActiveConnections");
        dataStorage.putRecord(endpointName, activeConnections);
    }

    @Scheduled(cron = "${email.trigger.cron}", zone = "${cron.time.zone}")
    public void sendEmail() {
        emailSender.sendNotification(dataStorage.getCurrentConnectionsNumber());
    }

    @Scheduled(cron = "${alert.trigger.cron}", zone = "${cron.time.zone}")
    public void checkForAlert() {
        if (dataStorage.checkForAlert()) {
            emailSender.sendAlertNotification(dataStorage.getCurrentConnectionsNumber());
        }
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void measureConnections() {
        logger.info("Current connections number: " + dataStorage.calculateTotalNumberOfConnections());
    }

    private String getTopicEnd(String destination) {
        String[] tokens = destination.split("/");
        return tokens[tokens.length - 1];
    }

}
