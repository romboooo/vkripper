package org.example.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jms")
@Data
public class FinanceNodeProperties {
    private String brokerUrl = "tcp://localhost:61616";
    private String financialOperationDestination = "Consumer.finance.VirtualTopic.financial-operation.requested";
    private String username;
    private String password;
}
