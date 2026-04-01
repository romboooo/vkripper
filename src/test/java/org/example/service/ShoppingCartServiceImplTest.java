package org.example.service;

import org.example.dto.response.ShoppingCartResponse;
import org.example.entity.Product;
import org.example.entity.ShoppingCart;
import org.example.entity.User;
import org.example.repository.ShoppingCartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ProductServiceImpl productService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private FavoriteServiceImpl favoriteService;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Test
    void shouldThrowExceptionWhenItemAlreadyInCart() {
        Long userId = 1L;
        Long productId = 100L;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("100.00"));
        product.setAvailable(true);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);
        when(shoppingCartRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

        assertThatThrownBy(() -> shoppingCartService.addToShoppingCart(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже в корзине");

        verify(shoppingCartRepository, never()).save(any());
    }
    @Test
    void shouldAddItemToCartSuccessfully() {
        Long userId = 1L;
        Long productId = 100L;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setAvailable(true);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);
        when(shoppingCartRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingCartResponse response = shoppingCartService.addToShoppingCart(userId, productId);

        assertThat(response).isNotNull();
        assertThat(response.getProduct().getId()).isEqualTo(productId);
        assertThat(response.getUser().getId()).isEqualTo(userId);
        assertThat(response.getAmount()).isEqualTo(1);

        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
    }
    @Test
    void shouldThrowExceptionWhenProductNotAvailable() {
        Long userId = 1L;
        Long productId = 100L;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setAvailable(false); // <--- Товар недоступен

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);

        // Act & Assert
        assertThatThrownBy(() -> shoppingCartService.addToShoppingCart(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("недоступен");

        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProductAmountSuccessfully() {
        Long userId = 1L;
        Long productId = 100L;
        int newAmount = 5;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setAvailable(true);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(10L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(1);

        when(shoppingCartRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(java.util.List.of(cartItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingCartResponse response = shoppingCartService.updateProductAmount(userId, productId, newAmount);

        assertThat(response.getAmount()).isEqualTo(newAmount);
        verify(shoppingCartRepository, times(1)).save(cartItem);
    }
}