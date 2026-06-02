package org.example.banking.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
public class JmsConfig {

    @Bean
    public ConnectionFactory connectionFactory(BankingNodeProperties properties) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getBrokerUrl());
        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            connectionFactory.setUserName(properties.getUsername());
        }
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            connectionFactory.setPassword(properties.getPassword());
        }
        return connectionFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        return factory;
    }
}
