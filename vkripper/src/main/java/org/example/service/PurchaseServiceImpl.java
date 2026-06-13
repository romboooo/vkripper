package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.entity.FinancialOperation;
import org.example.common.entity.Payment;
import org.example.common.entity.PurchaseOrder;
import org.example.common.enums.FinancialOperationStatus;
import org.example.common.enums.FinancialOperationType;
import org.example.common.enums.PaymentStatus;
import org.example.common.enums.PurchaseOrderStatus;
import org.example.common.repository.FinancialOperationRepository;
import org.example.common.repository.PaymentRepository;
import org.example.common.repository.PurchaseOrderRepository;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;
import org.example.entity.*;
import org.example.messaging.FinancialOperationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
    private final ShoppingCartServiceImpl shoppingCartService;
    private final UserServiceImpl userService;
    private final TransactionTemplate transactionTemplate;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PaymentRepository paymentRepository;
    private final FinancialOperationRepository financialOperationRepository;
    private final FinancialOperationEventPublisher financialOperationEventPublisher;

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    public PurchaseResponse createPurchase(Long buyerId, PurchaseRequest request) {
        User buyer = userService.getUserEntityById(buyerId);
        ShoppingCart cartItem = shoppingCartService.getCartEntityById(request.getCartItemId());
        User seller = cartItem.getProduct().getSeller();

        if (cartItem.getUser().getId() != buyer.getId()) {
            throw new RuntimeException("Элемент корзины не принадлежит пользователю");
        }

        Product product = cartItem.getProduct();
        BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(request.getAmountInPurchase()));

        return switch (request.getPurchaseType()) {
            case SELLER -> handleSellerPurchase(buyer, product, totalCost);
            case OZON -> handleOzonPurchase(buyer, product);
            case BALANCE -> handleBalancePurchase(
                    buyer.getId(),
                    seller.getId(),
                    totalCost,
                    request.getAmountInPurchase(),
                    cartItem.getId()
            );
        };
    }

    public void validatePurchaseForProcess(
            Long buyerId,
            Long cartItemId,
            PurchaseType purchaseType,
            Integer amountInPurchase
    ) {
        if (buyerId == null) {
            throw new IllegalArgumentException("buyerId is required");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("cartItemId is required");
        }
        if (purchaseType == null) {
            throw new IllegalArgumentException("purchaseType is required");
        }
        if (amountInPurchase == null || amountInPurchase < 1) {
            throw new IllegalArgumentException("amountInPurchase must be greater than 0");
        }

        User buyer = userService.getUserEntityById(buyerId);
        ShoppingCart cartItem = shoppingCartService.getCartEntityById(cartItemId);

        if (cartItem.getUser().getId() != buyer.getId()) {
            throw new RuntimeException("Элемент корзины не принадлежит пользователю");
        }

        if (purchaseType == PurchaseType.BALANCE) {
            int currentAmount = cartItem.getAmountInCart();
            if (amountInPurchase > currentAmount) {
                throw new RuntimeException("В корзине недостаточно товара. Запрошено: " +
                        amountInPurchase + ", доступно: " + currentAmount);
            }

            Product product = cartItem.getProduct();
            BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(amountInPurchase));
            if (buyer.getBalance().compareTo(totalCost) < 0) {
                throw new RuntimeException("Недостаточно средств. Требуется: " + totalCost +
                        ", Доступно: " + buyer.getBalance());
            }
        }
    }

    public BalancePurchaseProcessStepResult createBalancePurchaseForProcess(
            Long buyerId,
            Long cartItemId,
            Integer amountInPurchase
    ) {
        if (amountInPurchase == null || amountInPurchase < 1) {
            throw new IllegalArgumentException("amountInPurchase must be greater than 0");
        }

        return transactionTemplate.execute(status -> {
            User persistentBuyer = userService.getUserEntityById(buyerId);
            ShoppingCart persistentCart = shoppingCartService.getCartEntityById(cartItemId);

            if (persistentCart.getUser().getId() != persistentBuyer.getId()) {
                throw new RuntimeException("Элемент корзины не принадлежит пользователю");
            }

            int currentAmount = persistentCart.getAmountInCart();
            if (amountInPurchase > currentAmount) {
                throw new RuntimeException("В корзине недостаточно товара. Запрошено: " +
                        amountInPurchase + ", доступно: " + currentAmount);
            }

            Product product = persistentCart.getProduct();
            User seller = product.getSeller();
            BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(amountInPurchase));
            if (persistentBuyer.getBalance().compareTo(totalCost) < 0) {
                throw new RuntimeException("Недостаточно средств. Требуется: " + totalCost +
                        ", Доступно: " + persistentBuyer.getBalance());
            }

            PurchaseOrder order = new PurchaseOrder();
            order.setBuyerId(persistentBuyer.getId());
            order.setSellerId(seller.getId());
            order.setCartItemId(persistentCart.getId());
            order.setAmount(totalCost);
            order.setCurrency("RUB");
            order.setStatus(PurchaseOrderStatus.PAYMENT_PENDING);
            order = purchaseOrderRepository.save(order);

            Payment payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setUserId(persistentBuyer.getId());
            payment.setAmount(totalCost);
            payment.setCurrency("RUB");
            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);

            FinancialOperation operation = new FinancialOperation();
            operation.setType(FinancialOperationType.PURCHASE);
            operation.setUserId(persistentBuyer.getId());
            operation.setCounterpartyUserId(seller.getId());
            operation.setOrderId(order.getId());
            operation.setPaymentId(payment.getId());
            operation.setAmount(totalCost);
            operation.setCurrency("RUB");
            operation.setStatus(FinancialOperationStatus.PENDING);
            operation = financialOperationRepository.save(operation);

            int newAmount = currentAmount - amountInPurchase;
            if (newAmount > 0) {
                shoppingCartService.updateProductAmount(
                        persistentBuyer.getId(),
                        persistentCart.getProduct().getId(),
                        newAmount
                );
            } else {
                shoppingCartService.deleteCartEntity(persistentCart);
            }

            return new BalancePurchaseProcessStepResult(order.getId(), payment.getId(), operation.getId(), operation);
        });
    }

    public void publishFinancialOperationForProcess(Long operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId is required");
        }

        transactionTemplate.executeWithoutResult(status -> {
            FinancialOperation operation = financialOperationRepository.findById(operationId)
                    .orElseThrow(() -> new RuntimeException("Финансовая операция не найдена: " + operationId));
            financialOperationEventPublisher.publishAfterCommit(operation);
        });
    }

    private PurchaseResponse handleSellerPurchase(User buyer, Product product, BigDecimal totalCost) {
        Long sellerId = product.getSeller().getId();
        return new PurchaseResponse(
                null,
                "PENDING",
                "Свяжитесь с продавцом (ID: " + sellerId + ") для оформления заказа",
                null
        );
    }

    private PurchaseResponse handleOzonPurchase(User user, Product product) {
        return new PurchaseResponse(
                null,
                "REDIRECT",
                "Вы перешли в Ozon",
                "https://ozon.ru/t/yCwkBpx"
        );
    }

    private PurchaseResponse handleBalancePurchase(Long buyerId, Long sellerId,
                                                   BigDecimal totalCost, int amount,
                                                   Long cartItemId) {
        BalancePurchaseProcessStepResult result = createBalancePurchaseForProcess(buyerId, cartItemId, amount);
        financialOperationEventPublisher.publishAfterCommit(result.operation());
        return new PurchaseResponse(
                result.orderId(),
                "PAYMENT_PENDING",
                "Покупка создана, платёж ожидает обработки",
                null
        );
    }
}
