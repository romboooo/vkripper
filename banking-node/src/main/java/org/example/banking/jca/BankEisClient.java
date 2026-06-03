package org.example.banking.jca;

public interface BankEisClient {
    BankPaymentResult processPayment(BankPaymentRequest request);
}
