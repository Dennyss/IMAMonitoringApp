package com.ima;

import com.ibm.ima.jms.ImaJmsException;
import com.ibm.ima.jms.ImaJmsFactory;
import com.ibm.ima.jms.ImaProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
@Component
public class IMAConnectionFactoryProvider implements InitializingBean {
    private static Logger logger = LogManager.getLogger(IMAConnectionFactoryProvider.class);
    private UserCredentialsConnectionFactoryAdapter connectionFactory;

    @Value("${ima.serverURL}")
    private String serverURL;

    @Value("${ima.serverPort}")
    private String serverPort;

    @Value("${ima.userName}")
    private String userName;

    @Value("${ima.password}")
    private String password;

    @Override
    public void afterPropertiesSet() {
        ConnectionFactory imaConnectionFactory = null;
        ImaProperties props = null;
        try {
            imaConnectionFactory = ImaJmsFactory.createConnectionFactory();
            props = (ImaProperties) imaConnectionFactory;
            props.put("Port", serverPort);
            props.put("Server", serverURL);
        } catch (JMSException e) {
            logger.error("Exception while IMA connection factory initializing.", e);
        }
        ImaJmsException[] errs = props.validate(ImaProperties.WARNINGS);
        if (errs != null) {
            // Display the validation errors
            for (int i = 0; i < errs.length; i++) {
                logger.error("IMA Shared Subscription validation errors" + errs[i]);
            }
        }

        connectionFactory = new UserCredentialsConnectionFactoryAdapter();
        connectionFactory.setTargetConnectionFactory(imaConnectionFactory);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);

        logger.info("IMA connection factory initialized successfully");
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

}
