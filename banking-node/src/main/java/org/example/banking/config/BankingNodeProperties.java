package org.example.banking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jms")
public class BankingNodeProperties {
    private String brokerUrl = "tcp://localhost:61616";
    private String paymentDestination = "Consumer.banking.VirtualTopic.payment.requested";
    private String username;
    private String password;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getPaymentDestination() {
        return paymentDestination;
    }

    public void setPaymentDestination(String paymentDestination) {
        this.paymentDestination = paymentDestination;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
