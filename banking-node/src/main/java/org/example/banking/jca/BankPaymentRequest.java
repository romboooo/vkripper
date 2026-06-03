package org.example.banking.jca;

import java.math.BigDecimal;

public record BankPaymentRequest(
        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String currency
) {
}
