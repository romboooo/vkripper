package org.example.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.Review;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
public class ReviewListResponse {
    private List<ReviewResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static ReviewListResponse fromPage(Page<Review> page){
        return new ReviewListResponse(
                page.getContent().stream().map(ReviewResponse::fromReview).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

}
