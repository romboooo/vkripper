package org.example.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('MODERATOR')")
    public ReviewResponse createReview(ReviewRequest request) {

        User user = userService.getUserEntityByUsername(request.getUsername());
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
    @PreAuthorize(
            "hasRole('MODERATOR') or hasRole('ADMIN') or " +
                    "(#reviewOwner == authentication.principal.username)"
    )
    public ReviewResponse updateReview(long reviewID, ReviewRequest request, String reviewOwner){
        Review review = reviewRepository.findById(reviewID)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден"));
        review.setText(request.getText());
        review.setRating(request.getRating());
        return ReviewResponse.fromReview(reviewRepository.save(review));
    }
    @Override
    @PreAuthorize(
            "hasRole('MODERATOR') or hasRole('ADMIN') or " +
                    "(#reviewOwner == authentication.principal.username)"
    )
    public void deleteReview(long reviewID, String reviewOwner){
        reviewRepository.delete(reviewRepository.findById(reviewID)
                .orElseThrow(() -> new RuntimeException("Отзыв с id " + reviewID + " не найден")));
    }
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getAllReviews(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ReviewListResponse.fromPage( reviewRepository.findAll(pageable));
    }
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getReviewsByProduct(long prodId, int page, int size){
        Product product = productService.getProductEntityById(prodId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ReviewListResponse.fromPage(reviewRepository.findByProduct(product,pageable));
    }

}
