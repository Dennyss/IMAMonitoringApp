package com.configuration;

import com.ima.IMAConnectionFactoryProvider;
import com.ima.IMAMessageListener;
import com.ima.ReconnectingJMSExceptionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Created by Denys Kovalenko on 10/14/2016.
 */
@Configuration
public class SpringConfiguration {
    @Value("${ima.topicName}")
    private String topicName;

    @Autowired
    private IMAConnectionFactoryProvider imaConnectionFactoryProvider;

    @Autowired
    private IMAMessageListener imaMessageListener;


    @Bean
    public SimpleMessageListenerContainer getEndpointListenerContainer() {
        SingleConnectionFactory singleConnectionFactory = new SingleConnectionFactory(imaConnectionFactoryProvider.getConnectionFactory());
        singleConnectionFactory.setClientId("MonitoringApp");
        singleConnectionFactory.setReconnectOnException(true);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setPubSubDomain(true);
        container.setExceptionListener(new ReconnectingJMSExceptionListener(singleConnectionFactory));
        container.setDestinationName(topicName);
        container.setConnectionFactory(singleConnectionFactory);
        container.setMessageListener(imaMessageListener);

        return container;
    }

}
