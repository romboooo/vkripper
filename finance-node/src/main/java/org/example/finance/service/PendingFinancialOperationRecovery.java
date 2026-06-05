package org.example.finance.service;

import org.example.common.entity.FinancialOperation;
import org.example.common.enums.FinancialOperationStatus;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.example.common.repository.FinancialOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class PendingFinancialOperationRecovery {
    private static final Logger log = LoggerFactory.getLogger(PendingFinancialOperationRecovery.class);

    private final FinancialOperationRepository financialOperationRepository;
    private final FinancialOperationProcessingService financialOperationProcessingService;

    public PendingFinancialOperationRecovery(
            FinancialOperationRepository financialOperationRepository,
            FinancialOperationProcessingService financialOperationProcessingService
    ) {
        this.financialOperationRepository = financialOperationRepository;
        this.financialOperationProcessingService = financialOperationProcessingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void processPendingOperations() {
        List<FinancialOperation> operations = financialOperationRepository.findByStatusIn(List.of(
                FinancialOperationStatus.PENDING,
                FinancialOperationStatus.RETRY_PENDING
        ));
        if (operations.isEmpty()) {
            return;
        }

        log.info("Recovering {} pending financial operation(s)", operations.size());
        for (FinancialOperation operation : operations) {
            financialOperationProcessingService.process(toEvent(operation));
        }
    }

    private FinancialOperationRequestedEvent toEvent(FinancialOperation operation) {
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
