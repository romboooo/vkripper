package org.example.service;

import org.example.common.entity.FinancialOperation;

public record BalancePurchaseProcessStepResult(
        Long orderId,
        Long paymentId,
        Long operationId,
        FinancialOperation operation
) {
    public BalancePurchaseProcessStepResult(Long orderId, Long paymentId, Long operationId) {
        this(orderId, paymentId, operationId, null);
    }
}
