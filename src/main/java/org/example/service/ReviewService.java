package org.example.service;

import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
public interface ReviewService {
    ReviewResponse createReview(Long userId, ReviewRequest request);
    ReviewResponse updateReview(long reviewID, ReviewRequest request);

    void deleteReview(long reviewID);

    ReviewListResponse getReviewsByProduct(long prodId, int page, int size);
}
