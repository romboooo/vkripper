package org.example.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.example.common.event.PaymentRequestedEvent;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MqttPaymentPublisher {
    private final ObjectMapper objectMapper;
    private final MqttProperties properties;

    public MqttPaymentPublisher(ObjectMapper objectMapper, MqttProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void publishPaymentRequested(PaymentRequestedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);

            try (MqttClient client = new MqttClient(
                    properties.getBrokerUrl(),
                    properties.getClientId(),
                    new MemoryPersistence()
            )) {
                client.connect(connectOptions());
                client.publish(properties.getPaymentTopic(), message);
            }
        } catch (JsonProcessingException | MqttException e) {
            throw new IllegalStateException("Failed to publish payment.requested event", e);
        }
    }

    private MqttConnectOptions connectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(true);
        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
        }
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            options.setPassword(properties.getPassword().toCharArray());
        }
        return options;
    }
}
