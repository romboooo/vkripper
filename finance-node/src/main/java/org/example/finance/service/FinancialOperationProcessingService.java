package org.example.finance.service;

import org.example.common.event.FinancialOperationRequestedEvent;

public interface FinancialOperationProcessingService {
    void process(FinancialOperationRequestedEvent event);
}
