package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
import org.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @Parameter(description = "Товар")
            @Valid
            @RequestBody
            ReviewRequest reviewRequest
            ){
        return ResponseEntity.ok(reviewService.createReview(reviewRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактировать отзыв")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "ID отзыва")
            @PathVariable Long id,
            @Parameter(description = "Товар")
            @Valid
            @RequestBody
            ReviewRequest reviewRequest,
            @AuthenticationPrincipal UserDetails userDetails

    ){
        return ResponseEntity.ok(reviewService.updateReview(id,reviewRequest, userDetails.getUsername()));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв")
    public ResponseEntity<String> deleteReview(
            @Parameter(description = "ID отзыва")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        reviewService.deleteReview(id, userDetails.getUsername());
        return ResponseEntity.ok("Review has been deleted");
    }

    @GetMapping
    @Operation(summary = "Получить все отзывы")
    public ResponseEntity<ReviewListResponse> getAllReviews(
            @Parameter(description = "Номер страницы (начинается с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице")
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(reviewService.getAllReviews(page,size));
    }
    @GetMapping("/product/{productId}")
    @Operation(summary = "Получить отзывы товара по id")
    public ResponseEntity<ReviewListResponse> getReviewsByProduct(
            @Parameter(description = "ID товара")
            @PathVariable Long productId,
            @Parameter(description = "Номер страницы (начинается с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, page, size));
    }
}
