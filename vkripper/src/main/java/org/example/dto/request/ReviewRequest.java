package org.example.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;
    private String text;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
