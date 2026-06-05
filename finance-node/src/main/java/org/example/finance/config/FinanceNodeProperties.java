package org.example.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jms")
public class FinanceNodeProperties {
    private String brokerUrl = "tcp://localhost:61616";
    private String financialOperationDestination = "Consumer.finance.VirtualTopic.financial-operation.requested";
    private String username;
    private String password;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getFinancialOperationDestination() {
        return financialOperationDestination;
    }

    public void setFinancialOperationDestination(String financialOperationDestination) {
        this.financialOperationDestination = financialOperationDestination;
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
