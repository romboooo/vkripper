package org.example.finance.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.example.finance.service.FinancialOperationProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class FinancialOperationRequestedListener {
    private static final Logger log = LoggerFactory.getLogger(FinancialOperationRequestedListener.class);

    private final ObjectMapper objectMapper;
    private final FinancialOperationProcessingService financialOperationProcessingService;

    public FinancialOperationRequestedListener(
            ObjectMapper objectMapper,
            FinancialOperationProcessingService financialOperationProcessingService
    ) {
        this.objectMapper = objectMapper;
        this.financialOperationProcessingService = financialOperationProcessingService;
    }

    @JmsListener(destination = "${app.jms.financial-operation-destination:Consumer.finance.VirtualTopic.financial-operation.requested}")
    public void onFinancialOperationRequested(Message message) {
        try {
            String payload = readPayload(message);
            FinancialOperationRequestedEvent event = objectMapper.readValue(payload, FinancialOperationRequestedEvent.class);
            log.info(
                    "Received financial-operation.requested eventId={}, operationId={}, type={}",
                    event.eventId(),
                    event.operationId(),
                    event.type()
            );
            financialOperationProcessingService.process(event);
        } catch (Exception e) {
            log.error("Failed to handle financial-operation.requested message", e);
            throw new IllegalStateException("Failed to handle financial-operation.requested message", e);
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
