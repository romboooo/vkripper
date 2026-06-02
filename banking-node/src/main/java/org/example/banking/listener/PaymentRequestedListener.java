package org.example.banking.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.banking.service.PaymentProcessingService;
import org.example.common.event.PaymentRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestedListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentRequestedListener.class);

    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;

    public PaymentRequestedListener(ObjectMapper objectMapper, PaymentProcessingService paymentProcessingService) {
        this.objectMapper = objectMapper;
        this.paymentProcessingService = paymentProcessingService;
    }

    @JmsListener(destination = "${app.jms.payment-destination}")
    public void onPaymentRequested(String payload) {
        try {
            PaymentRequestedEvent event = objectMapper.readValue(payload, PaymentRequestedEvent.class);
            log.info(
                    "Received payment.requested eventId={}, paymentId={}, orderId={}",
                    event.eventId(),
                    event.paymentId(),
                    event.orderId()
            );
            paymentProcessingService.process(event);
        } catch (Exception e) {
            log.error("Failed to handle payment.requested message", e);
        }
    }
}
