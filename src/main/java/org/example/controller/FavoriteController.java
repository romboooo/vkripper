package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.FavoriteRequest;
import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.security.CustomUserDetails;
import org.example.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Избранное", description = "Работа с избранным")
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Получить список избранного", description = "Возвращает список избранных товаров")
    public ResponseEntity<FavoriteListResponse> getFavorites(
            @Parameter(description = "Номер страницы (начиная с нуля)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(favoriteService.getCatalog(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар из избранного", description = "Возвращает товар из избранного")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID товара")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(favoriteService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Добавить товар в избранное", description = "Добавляет товар в избранное текущего пользователя")
    public ResponseEntity<ProductResponse> addToFavorite(
            @Parameter(description = "Товар")
            @RequestBody FavoriteRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ProductResponse response = favoriteService.addToFavorite(currentUser.getId(), request.getProductId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Удалить товар из избранного", description = "Удаляет товар из избранного текущего пользователя")
    public ResponseEntity<String> deleteFromFavorite(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        favoriteService.deleteFromFavorite(currentUser.getId(), productId);
        return ResponseEntity.ok("Удалили товар с id " + productId + " от пользователя с id " + currentUser.getId());
    }
}