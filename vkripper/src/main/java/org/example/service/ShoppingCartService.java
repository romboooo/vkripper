package org.example.service;

import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.entity.ShoppingCart;
import org.example.entity.User;

import java.time.LocalDateTime;

public interface ShoppingCartService {

    ShoppingCartListResponse getByUserId(Long userId, int page, int size);

    ProductResponse getProductById(Long productId, Long userId);

    ShoppingCartResponse addToShoppingCart(Long userId, Long productId);

    void deleteFromShoppingCart(Long userId, Long productId);

    void removeAllFromShoppingCart(Long userId);

    void addToFavoriteFromCart(Long userId, Long productId);

    ShoppingCartResponse updateProductAmount(Long userId, Long productId, int newAmount);

    ShoppingCart getCartEntityById(Long id);

    void deleteCartEntity(ShoppingCart cartItem);

    int deleteOldShoppingCarts(LocalDateTime threshold);
}
