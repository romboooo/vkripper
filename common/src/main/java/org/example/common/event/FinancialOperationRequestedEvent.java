package org.example.common.event;

import org.example.common.enums.FinancialOperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialOperationRequestedEvent(
        UUID eventId,
        Long operationId,
        FinancialOperationType type,
        Long userId,
        Long counterpartyUserId,
        Long orderId,
        Long paymentId,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
