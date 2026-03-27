package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.request.ShoppingCartRequest;
import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/shoppingCart")
@Tag(name ="Корзина", description = "работа с корзиной товаров")
public class ShoppingCartController {
    public final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService){
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping
    @Operation(summary = "Получить товары в корзине пользователя", description = "Возвращает список товаров из корзины пользователя с пагинацией")
    public ResponseEntity<ShoppingCartListResponse> getShoppingCart(
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Parameter(description = "Номер страницы (начинается с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(shoppingCartService.getByUserId(userId, page, size));
    }


    @GetMapping("/{id}")
    @Operation(summary = "получить карточку товара по id", description = "возвращает товар по id")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID товара")
              Long productId,
            @Parameter(description = "ID пользователя")
             Long userId
    ){
        return ResponseEntity.ok(shoppingCartService.getProductById(productId, userId));
    }

    @PostMapping
    @Operation(summary = "добавить товар в корзину", description = "добавляет товар в корзину")
    public ResponseEntity<ShoppingCartResponse> addToShopping(
            @Parameter(description = "ID пользователя и ID товара")
            @RequestBody ShoppingCartRequest request
    ){
        ShoppingCartResponse response = shoppingCartService.addToShoppingCart(
                request.getUserId(), request.getProductId()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Удалить товар из корзины пользователя", description = "Удаляет товар из корзины конкретного пользователя")
    public ResponseEntity<String> removeFromShopping(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId
    ){
        shoppingCartService.deleteFromShoppingCart(userId, productId);
        return ResponseEntity.ok("Удалили товар с id  " + productId + " для пользователя с id" + userId);
    }

    @DeleteMapping("/all")
    @Operation(summary = "Очистить корзину пользователя", description = "Удаляет все товары из корзины конкретного пользователя")
    public ResponseEntity<String> removeAllFromShoppingCart(
            @Parameter(description = "ID пользователя") @RequestParam Long userId
    ){
        shoppingCartService.removeAllFromShoppingCart(userId);
        return ResponseEntity.ok("Все товары удалились из корзины для пользователя с ID" + userId);
    }

    @PostMapping("/favoriteFromCart")
    @Operation(summary = "Добавить товар из корзины в избранное", description = "Добавляет товар из корзины конкретного пользователя в избранное")
    public ResponseEntity<String> addToFavoriteFromCart(
            @Parameter(description = "id пользователя")
            @RequestParam Long userId,
            @Parameter(description = "id товара")
            @RequestParam Long productId
    ){
        shoppingCartService.addToFavoriteFromCart(userId, productId);
        return ResponseEntity.ok("Товар с id " + productId + " добавлен в избранное к пользователю с id " + userId);
    }

    @PutMapping("/updateAmount")
    @Operation(summary = "Изменить количество товара в корзине", description = "Обновляет количество конкретного товара в корзине пользователя")
    public ResponseEntity<ShoppingCartResponse> updateCartItemAmount(
            @Parameter(description = "id пользователя")
            @RequestParam Long userId,
            @Parameter(description = "id товара")
            @RequestParam Long productId,
            @Parameter(description = "новое количество товара")
            @RequestParam int newAmount
    ){
        ShoppingCartResponse response = shoppingCartService.updateProductAmount(userId, productId, newAmount);
        return ResponseEntity.ok(response);
    }
}
