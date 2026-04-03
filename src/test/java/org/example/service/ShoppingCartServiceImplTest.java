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
        product.setAvailable(false);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);

        assertThatThrownBy(() -> shoppingCartService.addToShoppingCart(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("недоступен");

        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProductAmountSuccessfully() {
        String username = "username";
        Long productId = 100L;
        int newAmount = 5;

        User user = new User();
        user.setId(username);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setAvailable(true);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(10L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(1);

        when(shoppingCartRepository.findByUsernameAndProductId(username, productId))
                .thenReturn(java.util.List.of(cartItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingCartResponse response = shoppingCartService.updateProductAmount(username, productId, newAmount);

        assertThat(response.getAmount()).isEqualTo(newAmount);
        verify(shoppingCartRepository, times(1)).save(cartItem);
    }
    @Test
    void shouldDeleteItemFromCartSuccessfully() {
        String username = "username";
        Long productId = 100L;

        User user = new User();
        user.setUsername(username);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(10L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(1);

        when(shoppingCartRepository.findByUsernameAndProductId(username, productId))
                .thenReturn(java.util.List.of(cartItem));

        shoppingCartService.deleteFromShoppingCart(username, productId);

        verify(shoppingCartRepository, times(1)).deleteAll(java.util.List.of(cartItem));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem() {
        Long userId = 10L;
        Long productId = 100L;

        when(shoppingCartRepository.findByUsernameAndProductId(userId, productId))
                .thenReturn(java.util.List.of());

        assertThatThrownBy(() -> shoppingCartService.deleteFromShoppingCart(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден в корзине");

        verify(shoppingCartRepository, never()).deleteAll(any());
    }
    @Test
    void shouldRemoveAllItemsFromCartSuccessfully() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);

        ShoppingCart cartItem1 = new ShoppingCart();
        cartItem1.setId(10L);
        cartItem1.setUser(user);
        cartItem1.setProduct(product);

        ShoppingCart cartItem2 = new ShoppingCart();
        cartItem2.setId(11L);
        cartItem2.setUser(user);
        cartItem2.setProduct(product);

        when(shoppingCartRepository.findAllByUserId(userId))
                .thenReturn(java.util.List.of(cartItem1, cartItem2));

        shoppingCartService.removeAllFromShoppingCart(userId);

        verify(shoppingCartRepository, times(1)).deleteAll(java.util.List.of(cartItem1, cartItem2));
    }

    @Test
    void shouldAddToFavoriteFromCartSuccessfully() {
        Long userId = 1L;
        Long productId = 100L;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setId(10L);
        cartItem.setUser(user);
        cartItem.setProduct(product);

        when(shoppingCartRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(java.util.List.of(cartItem));

        shoppingCartService.addToFavoriteFromCart(userId, productId);

        verify(favoriteService, times(1)).addFavoriteEntity(user, product);
    }

}