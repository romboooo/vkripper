package org.example.service;

import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;
import org.example.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

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
        doNothing().when(userService).saveUser(buyer);
        doNothing().when(userService).saveUser(seller);
        when(shoppingCartService.updateProductAmount(eq(userId), eq(cartId), eq(0)))
                .thenReturn(null);

        PurchaseResponse response = purchaseService.createPurchase(userId, request);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getMessage()).isEqualTo("Покупка успешно оформлена");
        assertThat(buyer.getBalance()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(seller.getBalance()).isEqualByComparingTo(new BigDecimal("200.00"));

        verify(userService).saveUser(buyer);
        verify(userService).saveUser(seller);
        verify(shoppingCartService).updateProductAmount(userId, cartId, 0);
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