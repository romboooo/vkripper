package org.example.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.entity.FinancialOperation;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinancialOperationEventPublisher {
    private final FinancialOperationRequestedEventFactory eventFactory;
    private final MqttFinancialOperationPublisher mqttPublisher;

    public void publishAfterCommit(FinancialOperation operation) {
        FinancialOperationRequestedEvent event = eventFactory.create(operation);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }

    private void publish(FinancialOperationRequestedEvent event) {
        try {
            mqttPublisher.publishFinancialOperationRequested(event);
        } catch (RuntimeException e) {
            log.error("Failed to publish financial operation event after commit, operationId={}", event.operationId(), e);
        }
    }
}
