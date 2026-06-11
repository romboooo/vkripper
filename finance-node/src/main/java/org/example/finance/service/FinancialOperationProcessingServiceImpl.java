package org.example.finance.service;

import org.example.common.entity.FinancialOperation;
import org.example.common.entity.Payment;
import org.example.common.entity.PurchaseOrder;
import org.example.common.enums.FinancialOperationStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.enums.PurchaseOrderStatus;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.example.common.repository.FinancialOperationRepository;
import org.example.common.repository.PaymentRepository;
import org.example.common.repository.PurchaseOrderRepository;
import org.example.finance.config.FinanceProcessingProperties;
import org.example.finance.jca.BankEisClient;
import org.example.finance.jca.BankPaymentRequest;
import org.example.finance.jca.BankPaymentResult;
import org.example.finance.jira.JiraIssueService;
import org.example.finance.repository.UserBalanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
public class FinancialOperationProcessingServiceImpl implements FinancialOperationProcessingService {
    private final FinancialOperationRepository financialOperationRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BankEisClient bankEisClient;
    private final UserBalanceRepository userBalanceRepository;
    private final FinanceProcessingProperties financeProcessingProperties;
    private final JiraIssueService jiraIssueService;

    public FinancialOperationProcessingServiceImpl(
            FinancialOperationRepository financialOperationRepository,
            PaymentRepository paymentRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            BankEisClient bankEisClient,
            UserBalanceRepository userBalanceRepository,
            FinanceProcessingProperties financeProcessingProperties,
            JiraIssueService jiraIssueService
    ) {
        this.financialOperationRepository = financialOperationRepository;
        this.paymentRepository = paymentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.bankEisClient = bankEisClient;
        this.userBalanceRepository = userBalanceRepository;
        this.financeProcessingProperties = financeProcessingProperties;
        this.jiraIssueService = jiraIssueService;
    }

    @Override
    @Transactional
    public void process(FinancialOperationRequestedEvent event) {
        FinancialOperation operation = financialOperationRepository.findById(event.operationId())
                .orElseThrow(() -> new IllegalArgumentException("Financial operation not found: " + event.operationId()));

        if (operation.getStatus() == FinancialOperationStatus.SUCCESS
                || operation.getStatus() == FinancialOperationStatus.FAILED) {
            return;
        }

        operation.setStatus(FinancialOperationStatus.PROCESSING);
        updateLinkedPaymentStatus(operation, PaymentStatus.PROCESSING);
        delayProcessing();

        BankPaymentResult result;
        try {
            result = bankEisClient.processPayment(new BankPaymentRequest(
                    operation.getId(),
                    operation.getOrderId(),
                    operation.getUserId(),
                    operation.getAmount(),
                    operation.getCurrency()
            ));
        } catch (RuntimeException e) {
            markRetryPending(operation, e);
            return;
        }

        operation.setProcessedAt(Instant.now());
        if (!result.success()) {
            markFailed(operation, result.errorCode(), result.errorMessage());
            return;
        }

        operation.setExternalTransactionId(result.externalTransactionId());
        switch (operation.getType()) {
            case TOP_UP -> processTopUp(operation);
            case WITHDRAW -> processWithdraw(operation);
            case PURCHASE -> processPurchase(operation);
        }
    }

    private void delayProcessing() {
        long delayMillis = financeProcessingProperties.getProcessingDelay().toMillis();
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Financial operation processing was interrupted", e);
        }
    }

    private void processTopUp(FinancialOperation operation) {
        if (!userBalanceRepository.credit(operation.getUserId(), operation.getAmount())) {
            markFailed(operation, "USER_NOT_FOUND", "User not found: " + operation.getUserId());
            return;
        }
        markSuccess(operation);
    }

    private void processWithdraw(FinancialOperation operation) {
        if (!userBalanceRepository.debitIfSufficientBalance(operation.getUserId(), operation.getAmount())) {
            markFailed(operation, "INSUFFICIENT_BALANCE", "User has insufficient balance: " + operation.getUserId());
            return;
        }
        markSuccess(operation);
    }

    private void processPurchase(FinancialOperation operation) {
        PurchaseOrder order = purchaseOrderRepository.findById(operation.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + operation.getOrderId()));

        if (!userBalanceRepository.debitIfSufficientBalance(order.getBuyerId(), order.getAmount())) {
            markFailed(operation, "INSUFFICIENT_BALANCE", "Buyer has insufficient balance for order " + order.getId());
            return;
        }

        if (!userBalanceRepository.credit(order.getSellerId(), order.getAmount())) {
            markFailed(operation, "SELLER_NOT_FOUND", "Seller not found for order " + order.getId());
            return;
        }

        order.setStatus(PurchaseOrderStatus.PAID);
        order.setPaidAt(Instant.now());
        markSuccess(operation);
    }

    private void markSuccess(FinancialOperation operation) {
        operation.setStatus(FinancialOperationStatus.SUCCESS);
        updateLinkedPayment(operation, PaymentStatus.SUCCESS, null, null, operation.getExternalTransactionId());
    }

    private void markFailed(FinancialOperation operation, String errorCode, String errorMessage) {
        operation.setStatus(FinancialOperationStatus.FAILED);
        operation.setErrorCode(errorCode);
        operation.setErrorMessage(errorMessage);
        operation.setProcessedAt(Instant.now());
        updateLinkedPayment(operation, PaymentStatus.FAILED, errorCode, errorMessage, null);
        updateLinkedOrderStatus(operation, PurchaseOrderStatus.PAYMENT_FAILED);
        createJiraIssue(operation, errorCode, errorMessage);
    }

    private void markRetryPending(FinancialOperation operation, RuntimeException e) {
        operation.setStatus(FinancialOperationStatus.RETRY_PENDING);
        operation.setRetryCount(operation.getRetryCount() + 1);
        operation.setErrorCode("TECHNICAL_ERROR");
        operation.setErrorMessage(e.getMessage());
        updateLinkedPayment(operation, PaymentStatus.RETRY_PENDING, "TECHNICAL_ERROR", e.getMessage(), null);
        createJiraIssue(operation, "TECHNICAL_ERROR", e.getMessage());
    }

    private void updateLinkedPaymentStatus(FinancialOperation operation, PaymentStatus status) {
        updateLinkedPayment(operation, status, null, null, null);
    }

    private void updateLinkedPayment(
            FinancialOperation operation,
            PaymentStatus status,
            String errorCode,
            String errorMessage,
            String externalTransactionId
    ) {
        if (operation.getPaymentId() == null) {
            return;
        }

        Payment payment = paymentRepository.findById(operation.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + operation.getPaymentId()));
        payment.setStatus(status);
        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED) {
            payment.setProcessedAt(Instant.now());
        }
        if (externalTransactionId != null) {
            payment.setExternalTransactionId(externalTransactionId);
        }
        if (errorCode != null) {
            payment.setErrorCode(errorCode);
        }
        if (errorMessage != null) {
            payment.setErrorMessage(errorMessage);
        }
        if (status == PaymentStatus.RETRY_PENDING) {
            payment.setRetryCount(payment.getRetryCount() + 1);
        }
    }

    private void updateLinkedOrderStatus(FinancialOperation operation, PurchaseOrderStatus status) {
        if (operation.getOrderId() == null) {
            return;
        }

        PurchaseOrder order = purchaseOrderRepository.findById(operation.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + operation.getOrderId()));
        order.setStatus(status);
    }

    private void createJiraIssue(FinancialOperation operation, String errorCode, String errorMessage) {
        try {
            jiraIssueService.createFinancialOperationProblemIssue(
                    operation,
                    errorCode,
                    errorMessage
            );
        } catch (RuntimeException jiraException) {
            log.warn("Failed to create Jira issue for operationId={}", operation.getId(), jiraException);
        }
    }
}
