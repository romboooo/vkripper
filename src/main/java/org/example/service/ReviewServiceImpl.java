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

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER')")
    @Transactional
    public ReviewResponse updateReview(Long reviewID, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewID).orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден"));
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

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ReviewListResponse getReviewsByProduct(Long prodId, int page, int size) {
        Product product = productService.getProductEntityById(prodId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ReviewListResponse.fromPage(reviewRepository.findByProduct(product, pageable));
    }
}