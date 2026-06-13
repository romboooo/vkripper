package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
import org.example.entity.Product;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserServiceImpl userService;
    private final ProductService productService;

    @Override
    @PreAuthorize("hasAuthority('BUYER') or (hasAuthority('SELLER') and @productServiceImpl.getProductEntityById(#request.productId).seller.id == #userId)")
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        User user = userService.getUserEntityById(userId);
        Product product = productService.getProductEntityById(request.getProductId());

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setText(request.getText());

        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromReview(saved);
    }

    public void validateReviewCreateForProcess(Long userId, String role, ReviewRequest request) {
        Product product = productService.getProductEntityById(request.getProductId());
        userService.getUserEntityById(userId);
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Оценка должна быть от 1 до 5");
        }
        boolean buyer = hasRole(role, "BUYER");
        boolean sellerOwnsProduct = hasRole(role, "SELLER") && product.getSeller().getId() == userId;
        if (!buyer && !sellerOwnsProduct) {
            throw new RuntimeException("Недостаточно прав для создания отзыва");
        }
    }

    @Transactional
    public ReviewResponse createReviewForProcess(Long userId, String role, ReviewRequest request) {
        validateReviewCreateForProcess(userId, role, request);
        User user = userService.getUserEntityById(userId);
        Product product = productService.getProductEntityById(request.getProductId());

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setText(request.getText());

        return ReviewResponse.fromReview(reviewRepository.save(review));
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER')")
    @Transactional
    public ReviewResponse updateReview(Long reviewID, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewID).orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден"));
        review.setText(request.getText());
        review.setRating(request.getRating());
        return ReviewResponse.fromReview(reviewRepository.save(review));
    }

    public void validateReviewUpdateForProcess(String role, Long reviewId, ReviewRequest request) {
        if (!hasRole(role, "BUYER") && !hasRole(role, "SELLER")) {
            throw new RuntimeException("Недостаточно прав для обновления отзыва");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Оценка должна быть от 1 до 5");
        }
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewId + " не найден"));
    }

    @Transactional
    public ReviewResponse updateReviewForProcess(String role, Long reviewId, ReviewRequest request) {
        validateReviewUpdateForProcess(role, reviewId, request);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewId + " не найден"));
        review.setText(request.getText());
        review.setRating(request.getRating());
        return ReviewResponse.fromReview(reviewRepository.save(review));
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    @Transactional
    public void deleteReview(Long reviewID) {
        reviewRepository.delete(reviewRepository.findById(reviewID).orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден")));
    }

    public void validateReviewDeleteForProcess(String role, Long reviewId) {
        if (!hasRole(role, "BUYER") && !hasRole(role, "MODERATOR") && !hasRole(role, "ADMIN")) {
            throw new RuntimeException("Недостаточно прав для удаления отзыва");
        }
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewId + " не найден"));
    }

    @Transactional
    public void deleteReviewForProcess(String role, Long reviewId) {
        validateReviewDeleteForProcess(role, reviewId);
        reviewRepository.delete(reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewId + " не найден")));
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ReviewListResponse getReviewsByProduct(Long prodId, int page, int size) {
        Product product = productService.getProductEntityById(prodId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ReviewListResponse.fromPage(reviewRepository.findByProduct(product, pageable));
    }

    private boolean hasRole(String actualRole, String expectedRole) {
        return expectedRole.equals(actualRole);
    }
}
