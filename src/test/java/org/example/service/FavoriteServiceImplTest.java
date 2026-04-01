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
        // Arrange
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

        // Act
        FavoriteListResponse response = favoriteService.getCatalog(0, 10);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldGetProductByIdFromFavoriteSuccessfully() {
        // Arrange
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

        // Act
        ProductResponse response = favoriteService.getProductById(10L);

        // Assert
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Test Product");
    }

    @Test
    void shouldThrowExceptionWhenFavoriteNotFound() {
        // Arrange
        when(favoriteRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> favoriteService.getProductById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Favorite not found");
    }

    @Test
    void shouldThrowExceptionWhenProductNotAvailable() {
        // Arrange
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

        // Act & Assert
        assertThatThrownBy(() -> favoriteService.getProductById(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void shouldAddToFavoriteSuccessfully() {
        // Arrange
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

        // Act
        ProductResponse response = favoriteService.addToFavorite(userId, productId);

        // Assert
        assertThat(response.getId()).isEqualTo(productId);
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void shouldThrowExceptionWhenProductAlreadyInFavorites() {
        // Arrange
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

        // Act & Assert
        assertThatThrownBy(() -> favoriteService.addToFavorite(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already in favorites");

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotAvailableForFavorite() {
        // Arrange
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

        // Act & Assert
        assertThatThrownBy(() -> favoriteService.addToFavorite(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void shouldDeleteFromFavoriteSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long productId = 100L;

        Favorite favorite = new Favorite();
        favorite.setId(10L);

        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(List.of(favorite));

        // Act
        favoriteService.deleteFromFavorite(userId, productId);

        // Assert
        verify(favoriteRepository, times(1)).deleteAll(List.of(favorite));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFavorite() {
        // Arrange
        Long userId = 1L;
        Long productId = 100L;

        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> favoriteService.deleteFromFavorite(userId, productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(favoriteRepository, never()).deleteAll(any());
    }

    @Test
    void shouldAddFavoriteEntitySuccessfully() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);
        product.setAvailable(true);

        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(List.of());
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        favoriteService.addFavoriteEntity(user, product);

        // Assert
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateFavoriteEntity() {
        // Arrange
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

        // Act & Assert
        assertThatThrownBy(() -> favoriteService.addFavoriteEntity(user, product))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже есть в избранном");
    }
}