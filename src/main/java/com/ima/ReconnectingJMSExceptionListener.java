package com.ima;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
public class ReconnectingJMSExceptionListener implements ExceptionListener {
    private static Logger logger = LogManager.getLogger(ReconnectingJMSExceptionListener.class);
    private ConnectionFactory connectionFactory;
    private int retryingInterval = 100;

    public ReconnectingJMSExceptionListener(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void onException(JMSException exception) {
        logger.error("Connection with broker lost");
        while (true) {
            try {
                Thread.sleep(retryingInterval);
                logger.error("Trying to reconnect to the broker ...");
                connectionFactory.createConnection();
                logger.info("Reconnected successfully!");
                // In case of successful reconnect, reset retryingInterval
                retryingInterval = 100;
                break;
            } catch (Exception e) {
                // Make sure retrying interval is limited by 10 sec
                retryingInterval = retryingInterval >= 10000 ? 10000 : retryingInterval * 2;
                logger.error("Reconnect attempt was not successful. Next reconnect after: " + retryingInterval + " millis ...");
            }
        }
    }
}
