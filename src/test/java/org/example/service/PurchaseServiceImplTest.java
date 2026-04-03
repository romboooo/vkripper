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
        Long userId = 1L;
        Long cartId = 10L;
        BigDecimal price = new BigDecimal("100.00");
        int amount = 2;

        User user = new User();
        user.setId(userId);
        user.setBalance(new BigDecimal("500.00"));

        Product product = new Product();
        product.setId(100L);
        product.setPrice(price);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(cartId);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(amount);

        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(userId);
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.BALANCE);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(user);
        doNothing().when(userService).saveUser(any(User.class));
        doNothing().when(shoppingCartService).deleteCartEntity(any(ShoppingCart.class));

        PurchaseResponse response = purchaseService.createPurchase(request);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getMessage()).isEqualTo("Покупка успешно оформлена");
        assertThat(user.getBalance()).isEqualByComparingTo(new BigDecimal("300.00"));

        verify(userService).saveUser(user);
        verify(shoppingCartService).deleteCartEntity(cartItem);
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        Long userId = 1L;
        Long cartId = 10L;
        BigDecimal price = new BigDecimal("100.00");
        int amount = 5;

        User user = new User();
        user.setId(userId);
        user.setBalance(new BigDecimal("100.00"));

        Product product = new Product();
        product.setId(100L);
        product.setPrice(price);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(cartId);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(amount);

        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(userId);
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.BALANCE);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(user);

        assertThatThrownBy(() -> purchaseService.createPurchase(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно средств");

        verify(userService, never()).saveUser(any());
        verify(shoppingCartService, never()).deleteCartEntity(any());
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
        cartItem.setAmount(1);

        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(userId);
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.SELLER);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(buyer);

        PurchaseResponse response = purchaseService.createPurchase(request);

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
        cartItem.setAmount(1);

        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(userId);
        request.setCartItemId(cartId);
        request.setPurchaseType(PurchaseType.OZON);

        when(shoppingCartService.getCartEntityById(cartId)).thenReturn(cartItem);
        when(userService.getUserEntityById(userId)).thenReturn(buyer);

        PurchaseResponse response = purchaseService.createPurchase(request);

        assertThat(response.getStatus()).isEqualTo("REDIRECT");
        assertThat(response.getRedirectUrl()).isEqualTo("https://ozon.ru/t/yCwkBpx");
    }
}