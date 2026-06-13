package org.example.jira;

import org.example.jira.adapter.api.JiraConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Configuration
public class JiraAdapterConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "app.jira-adapter",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public JiraConnectionFactory jiraConnectionFactory(JiraAdapterProperties properties) {
        try {
            return (JiraConnectionFactory) new InitialContext()
                    .lookup(properties.getConnectionFactoryJndiName());
        } catch (NamingException e) {
            throw new IllegalStateException(
                    "Failed to lookup JiraConnectionFactory by JNDI name: "
                            + properties.getConnectionFactoryJndiName(),
                    e
            );
        }
    }
}
