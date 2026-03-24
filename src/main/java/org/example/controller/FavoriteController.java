package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name= "Избранное", description = "Работа с избранным")
@RequestMapping("/api/favorite")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }


    @GetMapping
    @Operation(summary = "получить список избранного", description = "возвращает список избранных товаров")
    public ResponseEntity<FavoriteListResponse> getFavorites(
            @Parameter(description = "Номер страницы (начинается с нуля)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "количество товаров на странице")
            @RequestParam(defaultValue = "20") int size)
    {
        return ResponseEntity.ok(favoriteService.getCatalog(page, size));
    }


    @GetMapping("/{id}")
    @Operation(summary = "получить товар из избранного", description = "возвращает товар из избранного")
    public ResponseEntity<ProductResponse> getProduct(
        @Parameter(description = "ID товара")
        @PathVariable Long id
    ){
        return ResponseEntity.ok(favoriteService.getById(id));
    }
}
