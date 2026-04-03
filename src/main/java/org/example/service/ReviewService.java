package org.example.service;

import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);

    ReviewResponse updateReview(long reviewID, ReviewRequest request, String reviewOwner);

    void deleteReview(long reviewID, String reviewOwner);

    ReviewListResponse getAllReviews(int page, int size);

    ReviewListResponse getReviewsByProduct(long prodId, int page, int size);
}
