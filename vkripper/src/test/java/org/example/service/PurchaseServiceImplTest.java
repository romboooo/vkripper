package org.example.service;

import org.example.common.entity.FinancialOperation;
import org.example.common.entity.Payment;
import org.example.common.entity.PurchaseOrder;
import org.example.common.event.FinancialOperationRequestedEvent;
import org.example.common.repository.FinancialOperationRepository;
import org.example.common.repository.PaymentRepository;
import org.example.common.repository.PurchaseOrderRepository;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;
import org.example.entity.*;
import org.example.messaging.FinancialOperationRequestedEventFactory;
import org.example.messaging.MqttFinancialOperationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FinancialOperationRepository financialOperationRepository;

    @Mock
    private FinancialOperationRequestedEventFactory financialOperationRequestedEventFactory;

    @Mock
    private MqttFinancialOperationPublisher mqttPaymentPublisher;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    void shouldCompletePurchaseWithBalance() {
        Long userId = 1L, cartId = 10L;
        BigDecimal price = new BigDecimal("100.00");
        int amount = 2;

        User buyer = new User();
        buyer.setId(userId);
        buyer.setBalance(new BigDecimal("500.00"));

        User seller = new User();
        seller.setId(99L);
        seller.setBalance(BigDecimal.ZERO);

        Product product = new Product();
        product.setId(100L);
        product.setPrice(price);
        product.setSeller(seller);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(cartId);
        cartItem.setUser(buyer);
        cartItem.setProduct(product);
        cartItem.setAmountInCart(amount);

        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.BALANCE);
        request.setAmountInPurchase(amount);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(buyer);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder order = invocation.getArgument(0);
            order.setId(555L);
            return order;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(777L);
            return payment;
        });
        when(financialOperationRepository.save(any(FinancialOperation.class))).thenAnswer(invocation -> {
            FinancialOperation operation = invocation.getArgument(0);
            operation.setId(888L);
            return operation;
        });
        FinancialOperationRequestedEvent event = new FinancialOperationRequestedEvent(
                UUID.randomUUID(),
                888L,
                org.example.common.enums.FinancialOperationType.PURCHASE,
                1L,
                99L,
                555L,
                777L,
                new BigDecimal("200.00"),
                "RUB",
                Instant.now()
        );
        when(financialOperationRequestedEventFactory.create(any(FinancialOperation.class))).thenReturn(event);

        PurchaseResponse response = purchaseService.createPurchase(userId, request);

        assertThat(response.getOrderId()).isEqualTo(555L);
        assertThat(response.getStatus()).isEqualTo("PAYMENT_PENDING");
        assertThat(response.getMessage()).isEqualTo("Покупка создана, платёж ожидает обработки");
        assertThat(buyer.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(seller.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(financialOperationRepository).save(any(FinancialOperation.class));
        verify(mqttPaymentPublisher).publishFinancialOperationRequested(event);
        verify(shoppingCartService).deleteCartEntity(cartItem);
        verify(userService, never()).saveUser(any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        Long userId = 1L;
        Long cartId = 10L;
        BigDecimal price = new BigDecimal("100.00");
        int amount = 5;

        User buyer = new User();
        buyer.setId(userId);
        buyer.setBalance(new BigDecimal("100.00"));

        User seller = new User();
        seller.setId(99L);

        Product product = new Product();
        product.setId(100L);
        product.setPrice(price);
        product.setSeller(seller);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(cartId);
        cartItem.setUser(buyer);
        cartItem.setProduct(product);
        cartItem.setAmountInCart(amount);

        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.BALANCE);
        request.setAmountInPurchase(amount);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(buyer);

        assertThatThrownBy(() -> purchaseService.createPurchase(userId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно средств");

        verify(userService, never()).saveUser(any());
        verify(shoppingCartService, never()).updateProductAmount(anyLong(), anyLong(), anyInt());
    }

    @Test
    void shouldReturnPendingForSellerPurchase() {
        Long userId = 1L;
        Long sellerId = 99L;
        Long cartId = 10L;

        User seller = new User();
        seller.setId(sellerId);
        seller.setUsername("SellerName");

        User buyer = new User();
        buyer.setId(userId);

        Product product = new Product();
        product.setId(100L);
        product.setPrice(new BigDecimal("50.00"));
        product.setSeller(seller);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(cartId);
        cartItem.setUser(buyer);
        cartItem.setProduct(product);
        cartItem.setAmountInCart(1);

        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.SELLER);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(buyer);

        PurchaseResponse response = purchaseService.createPurchase(userId,request);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getMessage()).contains(String.valueOf(sellerId));
        verify(userService, never()).saveUser(any());
    }

    @Test
    void shouldReturnRedirectForOzonPurchase() {
        Long userId = 1L;
        Long cartId = 10L;

        User buyer = new User();
        buyer.setId(userId);

        Product product = new Product();
        product.setId(100L);
        product.setPrice(new BigDecimal("50.00"));

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(cartId);
        cartItem.setUser(buyer);
        cartItem.setProduct(product);
        cartItem.setAmountInCart(1);

        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.OZON);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(buyer);

        PurchaseResponse response = purchaseService.createPurchase(userId,request);

        assertThat(response.getStatus()).isEqualTo("REDIRECT");
        assertThat(response.getRedirectUrl()).isEqualTo("https://ozon.ru/t/yCwkBpx");
    }
}
