package org.example.banking.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.example.banking.service.PaymentProcessingService;
import org.example.common.event.PaymentRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PaymentRequestedListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentRequestedListener.class);

    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;

    public PaymentRequestedListener(ObjectMapper objectMapper, PaymentProcessingService paymentProcessingService) {
        this.objectMapper = objectMapper;
        this.paymentProcessingService = paymentProcessingService;
    }

    @JmsListener(destination = "${app.jms.payment-destination:Consumer.banking.VirtualTopic.payment.requested}")
    public void onPaymentRequested(Message message) {
        try {
            String payload = readPayload(message);
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
            throw new IllegalStateException("Failed to handle payment.requested message", e);
        }
    }

    private String readPayload(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            return textMessage.getText();
        }
        if (message instanceof BytesMessage bytesMessage) {
            byte[] payload = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(payload);
            return new String(payload, StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Unsupported JMS message type: " + message.getClass().getName());
    }
}
