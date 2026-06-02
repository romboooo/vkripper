package org.example.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProperties {
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "vkripper";
    private String paymentTopic = "payment.requested";
    private String username;
    private String password;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPaymentTopic() {
        return paymentTopic;
    }

    public void setPaymentTopic(String paymentTopic) {
        this.paymentTopic = paymentTopic;
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
