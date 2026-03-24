package org.example.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
import org.example.entity.Product;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewResponse createReview(ReviewRequest request) {
        long userId = request.getUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("Пользователь с id " + userId + " не найден"));

        long prodId = request.getProductId();
        Product product = productRepository.findById(prodId)
                .orElseThrow(
                        () -> new RuntimeException("Товар с id " + prodId + " не найден")
                );

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setText(request.getText());
        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromReview(saved);
    }

    public ReviewResponse updateReview(long reviewID, ReviewRequest request){
        Review review = reviewRepository.findById(reviewID)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден"));
        review.setText(request.getText());
        review.setRating(request.getRating());
        return ReviewResponse.fromReview(reviewRepository.save(review));
    }

    public void deleteReview(long reviewID){
        reviewRepository.delete(reviewRepository.findById(reviewID)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден")));
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getAllReviews(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ReviewListResponse.fromPage( reviewRepository.findAll(pageable));
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getReviewsByProduct(long prodId, int page, int size){
        Product product = productRepository.findById(prodId)
                .orElseThrow(
                        () -> new RuntimeException("Товар с id " + prodId + " не найден")
                );

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ReviewListResponse.fromPage(reviewRepository.findByProduct(product,pageable));
    }

}
