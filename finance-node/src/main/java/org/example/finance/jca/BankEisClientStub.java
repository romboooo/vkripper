package org.example.finance.jca;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BankEisClientStub implements BankEisClient {

    @Override
    public BankPaymentResult processPayment(BankPaymentRequest request) {
        return BankPaymentResult.success("stub-" + UUID.randomUUID());
    }
}
