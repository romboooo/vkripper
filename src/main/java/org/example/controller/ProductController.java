package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.response.ProductListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.ProductGroup;
import org.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Товары", description = "Работа с каталогом товаров")
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }
    @GetMapping
    @Operation(summary = "Получить каталог товаров", description = "Возвращает список доступных товаров")
    public ResponseEntity<ProductListResponse> getCatalog(
            @Parameter(description = "Номер страницы (начинается с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getCatalog(page, size));
    }

    @GetMapping("/group")
    @Operation(summary = "Фильтр по категории", description = "Возвращает товары указанной группы, которые есть в наличии")
    public ResponseEntity<ProductListResponse> getByGroup(
            @Parameter(description = "Группа товаров: ELECTRONICS, WARDROBE и т.д.")
            @RequestParam ProductGroup group,
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProductsByGroup(group, page, size));
    }


    @GetMapping("/search")
    @Operation(summary = "Поиск товаров", description = "Поиск по названию в доступных товарах")
    public ResponseEntity<java.util.List<ProductResponse>> search(
            @Parameter(description = "Поисковый запрос")
            @RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Карточка товара", description = "Полная информация о товаре по ID")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID товара")
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}