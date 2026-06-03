package org.example.banking.service;

import org.example.common.event.PaymentRequestedEvent;

public interface PaymentProcessingService {
    void process(PaymentRequestedEvent event);
}
