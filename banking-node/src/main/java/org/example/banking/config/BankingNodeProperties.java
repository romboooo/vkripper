package org.example.banking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jms")
@Data
public class BankingNodeProperties {
    private String brokerUrl = "tcp://localhost:61616";
    private String paymentDestination = "payment.requested";
    private String username;
    private String password;

}
