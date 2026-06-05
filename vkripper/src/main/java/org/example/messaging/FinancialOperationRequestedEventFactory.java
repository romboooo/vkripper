package org.example.messaging;

import org.example.common.entity.FinancialOperation;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class FinancialOperationRequestedEventFactory {

    public FinancialOperationRequestedEvent create(FinancialOperation operation) {
        return new FinancialOperationRequestedEvent(
                UUID.randomUUID(),
                operation.getId(),
                operation.getType(),
                operation.getUserId(),
                operation.getCounterpartyUserId(),
                operation.getOrderId(),
                operation.getPaymentId(),
                operation.getAmount(),
                operation.getCurrency(),
                Instant.now()
        );
    }
}
