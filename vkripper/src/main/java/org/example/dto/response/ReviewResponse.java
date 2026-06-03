package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.Review;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private String text;
    private Integer rating;
    private LocalDateTime createdAt;

    public static ReviewResponse fromReview(Review review){
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getProduct().getId(),
                review.getText(),
                review.getRating(),
                review.getCreatedAt()
        );
    }

}
