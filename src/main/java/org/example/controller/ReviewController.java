package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
import org.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/reviews")
@Tag(name = "Отзывы", description = "Управление отзывами")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Добавить отзыв")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid
            @RequestBody
            ReviewRequest reviewRequest
            ){
        return ResponseEntity.ok(reviewService.createReview(reviewRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактировать отзыв")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid
            @RequestBody
            ReviewRequest reviewRequest

    ){
        return ResponseEntity.ok(reviewService.updateReview(id,reviewRequest));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long id
    ){
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review has been deleted");
    }

    @GetMapping
    @Operation(summary = "Получить все отзывы")
    public ResponseEntity<ReviewListResponse> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(reviewService.getAllReviews(page,size));
    }
    @GetMapping("/product/{productId}")
    @Operation(summary = "Получить отзывы товара по id")
    public ResponseEntity<ReviewListResponse> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, page, size));
    }
}
