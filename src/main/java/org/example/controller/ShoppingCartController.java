package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.response.ShoppingCartResponse;
import org.example.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingCart")
@Tag(name ="Корзина", description = "работа с корзиной товаров")
public class ShoppingCartController {
    public final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService){
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "получить товары в корзине пользователя", description = "возвращает список товаров из корзины пользователя")
    public ResponseEntity<List<ShoppingCartResponse>> getShoppingCart(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id
    ){
        return ResponseEntity.ok(shoppingCartService.getByUserId(id));
    }
}
