package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewListResponse;
import org.example.dto.response.ReviewResponse;
import org.example.security.CustomUserDetails;
import org.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Tag(name = "Отзывы", description = "Управление отзывами")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Добавить отзыв", description = "Создает отзыв от имени текущего пользователя")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reviewService.createReview(currentUser.getId(), reviewRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактировать отзыв", description = "Обновляет текст и оценку отзыва")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "ID отзыва") @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest
    ) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв по ID")
    public ResponseEntity<String> deleteReview(
            @Parameter(description = "ID отзыва") @PathVariable Long id
    ) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Отзыв удален");
    }

    @GetMapping
    @Operation(summary = "Получить все отзывы", description = "Возвращает список всех отзывов с пагинацией")
    public ResponseEntity<ReviewListResponse> getAllReviews(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество отзывов на странице") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(page, size));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Получить отзывы товара", description = "Возвращает отзывы конкретного товара с пагинацией")
    public ResponseEntity<ReviewListResponse> getReviewsByProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество отзывов на странице") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, page, size));
    }
}