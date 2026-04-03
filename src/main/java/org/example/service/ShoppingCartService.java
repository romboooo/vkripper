package org.example.service;

import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.entity.ShoppingCart;

public interface ShoppingCartService {

    ShoppingCartListResponse getByUsername(String username, int page, int size);

    ProductResponse getProductById(Long productId, String username);

    ShoppingCartResponse addToShoppingCart(String username, Long productId);

    void deleteFromShoppingCart(String username, Long productId);

    void removeAllFromShoppingCart(String username);

    void addToFavoriteFromCart(String username, Long productId);

    ShoppingCartResponse updateProductAmount(String username, Long productId, int newAmount);

    ShoppingCart getCartEntityById(Long id);

    void deleteCartEntity(ShoppingCart cartItem);
}
