package org.example.banking.service;

import org.example.banking.jca.BankEisClient;
import org.example.banking.jca.BankPaymentRequest;
import org.example.banking.jca.BankPaymentResult;
import org.example.common.entity.Payment;
import org.example.common.entity.PurchaseOrder;
import org.example.common.enums.PaymentStatus;
import org.example.common.enums.PurchaseOrderStatus;
import org.example.common.event.PaymentRequestedEvent;
import org.example.common.repository.PaymentRepository;
import org.example.common.repository.PurchaseOrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PaymentProcessingServiceImpl implements PaymentProcessingService {
    private final PaymentRepository paymentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BankEisClient bankEisClient;
    private final JdbcTemplate jdbcTemplate;

    public PaymentProcessingServiceImpl(
            PaymentRepository paymentRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            BankEisClient bankEisClient,
            JdbcTemplate jdbcTemplate
    ) {
        this.paymentRepository = paymentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.bankEisClient = bankEisClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void process(PaymentRequestedEvent event) {
        Payment payment = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + event.paymentId()));

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        PurchaseOrder order = purchaseOrderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + payment.getOrderId()));

        payment.setStatus(PaymentStatus.PROCESSING);

        BankPaymentResult result;
        try {
            result = bankEisClient.processPayment(new BankPaymentRequest(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getCurrency()
            ));
        } catch (RuntimeException e) {
            payment.setStatus(PaymentStatus.RETRY_PENDING);
            payment.setRetryCount(payment.getRetryCount() + 1);
            payment.setErrorCode("TECHNICAL_ERROR");
            payment.setErrorMessage(e.getMessage());
            return;
        }

        payment.setProcessedAt(Instant.now());
        if (result.success()) {
            transferBalance(order);
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setExternalTransactionId(result.externalTransactionId());
            order.setStatus(PurchaseOrderStatus.PAID);
            order.setPaidAt(Instant.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorCode(result.errorCode());
            payment.setErrorMessage(result.errorMessage());
            order.setStatus(PurchaseOrderStatus.PAYMENT_FAILED);
        }
    }

    private void transferBalance(PurchaseOrder order) {
        int buyerRows = jdbcTemplate.update(
                "update users set balance = balance - ? where id = ? and balance >= ?",
                order.getAmount(),
                order.getBuyerId(),
                order.getAmount()
        );
        if (buyerRows != 1) {
            throw new IllegalStateException("Buyer has insufficient balance for order " + order.getId());
        }

        int sellerRows = jdbcTemplate.update(
                "update users set balance = balance + ? where id = ?",
                order.getAmount(),
                order.getSellerId()
        );
        if (sellerRows != 1) {
            throw new IllegalStateException("Seller not found for order " + order.getId());
        }
    }
}
