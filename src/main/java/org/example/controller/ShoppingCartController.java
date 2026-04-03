package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.request.ShoppingCartRequest;
import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails,
            @Parameter(description = "Номер страницы (начинается с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество товаров на странице") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(shoppingCartService.getByUsername(userDetails.getUsername(), page, size));
    }


    @GetMapping("/{id}")
    @Operation(summary = "получить карточку товара по id", description = "возвращает товар по id")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID товара")
              Long productId,
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails
    ){
        return ResponseEntity.ok(shoppingCartService.getProductById(productId, userDetails.getUsername()));
    }

    @PostMapping
    @Operation(summary = "добавить товар в корзину", description = "добавляет товар в корзину")
    public ResponseEntity<ShoppingCartResponse> addToShopping(
            @Parameter(description = "username пользователя и ID товара")
            @RequestBody ShoppingCartRequest request,
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails
    ){
        ShoppingCartResponse response = shoppingCartService.addToShoppingCart(
                userDetails.getUsername(), request.getProductId()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Удалить товар из корзины пользователя", description = "Удаляет товар из корзины конкретного пользователя")
    public ResponseEntity<String> removeFromShopping(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails
    ){
        shoppingCartService.deleteFromShoppingCart(userDetails.getUsername(), productId);
        return ResponseEntity.ok("Удалили товар с id  " + productId + " для пользователя с id" + userDetails.getUsername());
    }

    @DeleteMapping("/all")
    @Operation(summary = "Очистить корзину пользователя", description = "Удаляет все товары из корзины конкретного пользователя")
    public ResponseEntity<String> removeAllFromShoppingCart(
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails
    ){
        shoppingCartService.removeAllFromShoppingCart(userDetails.getUsername());
        return ResponseEntity.ok("Все товары удалились из корзины для пользователя с ID" + userDetails.getUsername());
    }

    @PostMapping("/favoriteFromCart")
    @Operation(summary = "Добавить товар из корзины в избранное", description = "Добавляет товар из корзины конкретного пользователя в избранное")
    public ResponseEntity<String> addToFavoriteFromCart(
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails,
            @Parameter(description = "id товара")
            @RequestParam Long productId
    ){
        shoppingCartService.addToFavoriteFromCart(userDetails.getUsername(), productId);
        return ResponseEntity.ok("Товар с id " + productId + " добавлен в избранное к пользователю с id " + userDetails.getUsername());
    }

    @PutMapping("/updateAmount")
    @Operation(summary = "Изменить количество товара в корзине", description = "Обновляет количество конкретного товара в корзине пользователя")
    public ResponseEntity<ShoppingCartResponse> updateCartItemAmount(
            @Parameter(description = "username пользователя")
            @AuthenticationPrincipal @Valid UserDetails userDetails,
            @Parameter(description = "id товара")
            @RequestParam Long productId,
            @Parameter(description = "новое количество товара")
            @RequestParam int newAmount
    ){
        ShoppingCartResponse response = shoppingCartService.updateProductAmount(userDetails.getUsername(), productId, newAmount);
        return ResponseEntity.ok(response);
    }
}
