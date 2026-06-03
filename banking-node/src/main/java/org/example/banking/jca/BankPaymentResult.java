package org.example.banking.jca;

public record BankPaymentResult(
        boolean success,
        String externalTransactionId,
        String errorCode,
        String errorMessage
) {
    public static BankPaymentResult success(String externalTransactionId) {
        return new BankPaymentResult(true, externalTransactionId, null, null);
    }

    public static BankPaymentResult failed(String errorCode, String errorMessage) {
        return new BankPaymentResult(false, null, errorCode, errorMessage);
    }
}
