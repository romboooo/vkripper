package org.example.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProperties {
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "vkripper";
    private String financialOperationTopic = "VirtualTopic/financial-operation/requested";
    private String username;
    private String password;

}
