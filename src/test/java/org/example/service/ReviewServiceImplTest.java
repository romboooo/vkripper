package org.example.service;

import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
import org.example.entity.Product;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ProductServiceImpl productService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void shouldCreateReviewSuccessfully() {
        Long userId = 1L;
        Long productId = 100L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setAvailable(true);

        ReviewRequest request = new ReviewRequest();
        request.setProductId(productId);
        request.setText("Great product!");
        request.setRating(5);

        Review savedReview = new Review();
        savedReview.setId(10L);
        savedReview.setUser(user);
        savedReview.setProduct(product);
        savedReview.setText("Great product!");
        savedReview.setRating(5);
        savedReview.setCreatedAt(java.time.LocalDateTime.now());

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(productService.getProductEntityById(productId)).thenReturn(product);

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getText()).isEqualTo("Great product!");
        assertThat(response.getRating()).isEqualTo(5);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void shouldUpdateReviewSuccessfully() {
        Long reviewId = 10L;

        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);

        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setUser(user);
        existingReview.setProduct(product);
        existingReview.setText("Old text");
        existingReview.setRating(3);

        ReviewRequest request = new ReviewRequest();
        request.setProductId(100L);
        request.setText("New text");
        request.setRating(5);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.updateReview(reviewId, request);

        assertThat(response.getText()).isEqualTo("New text");
        assertThat(response.getRating()).isEqualTo(5);
        verify(reviewRepository, times(1)).save(existingReview);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentReview() {
        Long reviewId = 999L;
        ReviewRequest request = new ReviewRequest();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(reviewId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void shouldDeleteReviewSuccessfully() {
        Long reviewId = 10L;

        Review review = new Review();
        review.setId(reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId);

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentReview() {
        Long reviewId = 999L;

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(reviewId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void shouldGetAllReviewsSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(100L);

        Review review1 = new Review();
        review1.setId(10L);
        review1.setUser(user);
        review1.setProduct(product);
        review1.setText("Review 1");
        review1.setRating(5);

        Review review2 = new Review();
        review2.setId(11L);
        review2.setUser(user);
        review2.setProduct(product);
        review2.setText("Review 2");
        review2.setRating(4);

        Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2));
        when(reviewRepository.findAll(any(PageRequest.class))).thenReturn(reviewPage);

        ReviewListResponse response = reviewService.getAllReviews(0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldGetReviewsByProductSuccessfully() {
        Long productId = 100L;

        User user = new User();
        user.setId(1L);
        user.setFavorites(new java.util.ArrayList<>());

        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setAvailable(true);

        Review review = new Review();
        review.setId(10L);
        review.setUser(user);
        review.setProduct(product);
        review.setText("Good");
        review.setRating(5);

        Page<Review> reviewPage = new PageImpl<>(List.of(review));

        when(productService.getProductEntityById(productId)).thenReturn(product);
        when(reviewRepository.findByProduct(eq(product), any(PageRequest.class))).thenReturn(reviewPage);

        ReviewListResponse response = reviewService.getReviewsByProduct(productId, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenGettingReviewsForNonExistentProduct() {
        Long productId = 999L;

        when(productService.getProductEntityById(productId))
                .thenThrow(new RuntimeException("Товар с id " + productId + " не найден"));

        assertThatThrownBy(() -> reviewService.getReviewsByProduct(productId, 0, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }
}