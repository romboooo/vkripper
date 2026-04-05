package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.request.ShoppingCartRequest;
import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.security.CustomUserDetails;
import org.example.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shoppingCart")
@Tag(name ="Корзина", description = "работа с корзиной товаров")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService){
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping
    @Operation(summary = "Получить список товаров в корзине", description = "Возвращает список товаров из корзины текущего пользователя с пагинацией")
    public ResponseEntity<ShoppingCartListResponse> getShoppingCart(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(shoppingCartService.getByUserId(currentUser.getId(), page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить карточку товара по id", description = "Возвращает товар из корзины текущего пользователя")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID товара") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        return ResponseEntity.ok(shoppingCartService.getProductById(id, currentUser.getId()));
    }

    @PostMapping
    @Operation(summary = "Добавить товар в корзину", description = "Добавляет товар в корзину текущего пользователя")
    public ResponseEntity<ShoppingCartResponse> addToShopping(
            @Parameter(description = "Данные для добавления (ID товара)")
            @RequestBody ShoppingCartRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        ShoppingCartResponse response = shoppingCartService.addToShoppingCart(
                currentUser.getId(), request.getProductId()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Удалить товар из корзины", description = "Удаляет товар из корзины текущего пользователя")
    public ResponseEntity<String> removeFromShopping(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        shoppingCartService.deleteFromShoppingCart(currentUser.getId(), productId);
        return ResponseEntity.ok("Удалили товар с id " + productId + " для пользователя с id " + currentUser.getId());
    }

    @DeleteMapping("/all")
    @Operation(summary = "Очистить корзину", description = "Удаляет все товары из корзины текущего пользователя")
    public ResponseEntity<String> removeAllFromShoppingCart(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        shoppingCartService.removeAllFromShoppingCart(currentUser.getId());
        return ResponseEntity.ok("Все товары удалились из корзины для пользователя с ID " + currentUser.getId());
    }

    @PostMapping("/favoriteFromCart")
    @Operation(summary = "Добавить товар из корзины в избранное", description = "Переносит товар из корзины текущего пользователя в избранное")
    public ResponseEntity<String> addToFavoriteFromCart(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "id товара") @RequestParam Long productId
    ){
        shoppingCartService.addToFavoriteFromCart(currentUser.getId(), productId);
        return ResponseEntity.ok("Товар с id " + productId + " добавлен в избранное к пользователю с id " + currentUser.getId());
    }

    @PutMapping("/updateAmount")
    @Operation(summary = "Изменить количество товара в корзине", description = "Обновляет количество конкретного товара в корзине текущего пользователя")
    public ResponseEntity<ShoppingCartResponse> updateCartItemAmount(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "id товара") @RequestParam Long productId,
            @Parameter(description = "новое количество товара") @RequestParam int newAmount
    ){
        ShoppingCartResponse response = shoppingCartService.updateProductAmount(currentUser.getId(), productId, newAmount);
        return ResponseEntity.ok(response);
    }
}