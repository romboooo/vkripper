package org.example.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MqttFinancialOperationPublisher {
    private static final Logger log = LoggerFactory.getLogger(MqttFinancialOperationPublisher.class);

    private final ObjectMapper objectMapper;
    private final MqttProperties properties;

    public MqttFinancialOperationPublisher(ObjectMapper objectMapper, MqttProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void publishFinancialOperationRequested(FinancialOperationRequestedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);

            MqttClient client = new MqttClient(
                    properties.getBrokerUrl(),
                    properties.getClientId(),
                    new MemoryPersistence()
            );
            client.connect(connectOptions());
            client.publish(properties.getFinancialOperationTopic(), message);
            client.disconnect();
            client.close();
        } catch (JsonProcessingException | MqttException e) {
            if (e instanceof MqttException mqttException) {
                log.error(
                        "Failed to publish financial-operation.requested event to brokerUrl={}, topic={}, reasonCode={}",
                        properties.getBrokerUrl(),
                        properties.getFinancialOperationTopic(),
                        mqttException.getReasonCode(),
                        mqttException
                );
            } else {
                log.error("Failed to serialize financial-operation.requested event", e);
            }
            throw new IllegalStateException("Failed to publish financial-operation.requested event", e);
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
