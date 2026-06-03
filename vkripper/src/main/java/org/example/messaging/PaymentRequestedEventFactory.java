package org.example.messaging;

import org.example.common.entity.Payment;
import org.example.common.event.PaymentRequestedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentRequestedEventFactory {

    public PaymentRequestedEvent create(Payment payment) {
        return new PaymentRequestedEvent(
                UUID.randomUUID(),
                payment.getOrderId(),
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                Instant.now()
        );
    }
}
