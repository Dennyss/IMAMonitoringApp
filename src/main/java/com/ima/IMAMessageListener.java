package com.ima;

import com.handler.DataHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
@Component
public class IMAMessageListener implements MessageListener {
    private static Logger logger = LogManager.getLogger(IMAMessageListener.class);

    @Autowired
    private DataHandler dataHandler;

    @Override
    public void onMessage(Message message) {

        if (message instanceof TextMessage) {
            String jsonPayload = null;
            String destination = null;
            try {
                jsonPayload = ((TextMessage) message).getText();
                destination = message.getJMSDestination().toString();
            } catch (JMSException e) {
                logger.error("Exception while parsing input JMS message text payload", e);
            }

            dataHandler.onMessage(jsonPayload, destination);
        } else {
            logger.warn("Input message is not TextMessage. Ignoring it...");
        }
    }
}
