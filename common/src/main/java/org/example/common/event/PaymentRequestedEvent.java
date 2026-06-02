package org.example.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRequestedEvent(
        UUID eventId,
        Long orderId,
        Long paymentId,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
