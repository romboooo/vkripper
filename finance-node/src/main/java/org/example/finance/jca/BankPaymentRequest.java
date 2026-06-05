package org.example.finance.jca;

import java.math.BigDecimal;

public record BankPaymentRequest(
        Long operationId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String currency
) {
}
