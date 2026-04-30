package org.example.service;

import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Favorite;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProductServiceImpl productService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Test
    void shouldGetCatalogSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);

        Favorite favorite = new Favorite();
        favorite.setId(10L);
        favorite.setUser(user);
        favorite.setProduct(product);

        Page<Favorite> favoritePage = new PageImpl<>(List.of(favorite));
        when(favoriteRepository.findAll(any(PageRequest.class))).thenReturn(favoritePage);

        FavoriteListResponse response = favoriteService.getFavorites(0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldGetProductByIdFromFavoriteSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);

        Favorite favorite = new Favorite();
        favorite.setId(10L);
        favorite.setUser(user);
        favorite.setProduct(product);

        when(favoriteRepository.findById(10L)).thenReturn(java.util.Optional.of(favorite));

        ProductResponse response = favoriteService.getProductById(10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Test Product");
    }

    @Test
    void shouldThrowExceptionWhenFavoriteNotFound() {
        when(favoriteRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> favoriteService.getProductById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Favorite not found");
    }

    @Test
    void shouldThrowExceptionWhenProductNotAvailable() {
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);
        product.setAvailable(false);

        Favorite favorite = new Favorite();
        favorite.setId(10L);
        favorite.setUser(user);
        favorite.setProduct(product);

        when(favoriteRepository.findById(10L)).thenReturn(java.util.Optional.of(favorite));

        assertThatThrownBy(() -> favoriteService.getProductById(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void shouldAddToFavoriteSuccessfully() {
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
        product.setProductGroup(ProductGroup.ELECTRONICS);

        Favorite favorite = new Favorite();
        favorite.setId(10L);
        favorite.setUser(user);
        favorite.setProduct(product);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);
        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(List.of());
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = favoriteService.addToFavorite(userId, productId);

        assertThat(response.getId()).isEqualTo(productId);
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void shouldThrowExceptionWhenProductAlreadyInFavorites() {
        Long userId = 1L;
        Long productId = 100L;

        User user = new User();
        user.setId(userId);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setAvailable(true);

        Favorite existingFavorite = new Favorite();
        existingFavorite.setId(10L);
        existingFavorite.setUser(user);
        existingFavorite.setProduct(product);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);
        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(List.of(existingFavorite));

        assertThatThrownBy(() -> favoriteService.addToFavorite(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already in favorites");

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotAvailableForFavorite() {
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

        assertThatThrownBy(() -> favoriteService.addToFavorite(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void shouldDeleteFromFavoriteSuccessfully() {
        Long userId = 1L;
        Long productId = 100L;

        Favorite favorite = new Favorite();
        favorite.setId(10L);

        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(List.of(favorite));

        favoriteService.deleteFromFavorite(userId, productId);

        verify(favoriteRepository, times(1)).deleteAll(List.of(favorite));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFavorite() {
        Long userId = 1L;
        Long productId = 100L;

        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(List.of());

        assertThatThrownBy(() -> favoriteService.deleteFromFavorite(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(favoriteRepository, never()).deleteAll(any());
    }

    @Test
    void shouldAddFavoriteEntitySuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);
        product.setAvailable(true);

        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(List.of());
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        favoriteService.addFavoriteEntity(user, product);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateFavoriteEntity() {
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);

        Favorite existingFavorite = new Favorite();
        existingFavorite.setId(10L);
        existingFavorite.setUser(user);
        existingFavorite.setProduct(product);

        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(List.of(existingFavorite));

        assertThatThrownBy(() -> favoriteService.addFavoriteEntity(user, product))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже есть в избранном");
    }
}