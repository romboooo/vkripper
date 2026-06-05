package org.example.finance.jca;

public interface BankEisClient {
    BankPaymentResult processPayment(BankPaymentRequest request);
}
