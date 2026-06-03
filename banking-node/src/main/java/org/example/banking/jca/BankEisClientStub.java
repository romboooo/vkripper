package org.example.banking.jca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BankEisClientStub implements BankEisClient {
    private final boolean forceFailure;

    public BankEisClientStub(@Value("${app.banking.stub.force-failure:false}") boolean forceFailure) {
        this.forceFailure = forceFailure;
    }

    @Override
    public BankPaymentResult processPayment(BankPaymentRequest request) {
        if (forceFailure) {
            return BankPaymentResult.failed("STUB_FORCED_FAILURE", "Forced banking failure for Jira integration test");
        }

        return BankPaymentResult.success("stub-" + UUID.randomUUID());
    }
}
