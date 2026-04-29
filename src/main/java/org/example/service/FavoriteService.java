package org.example.service;

import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.User;

public interface FavoriteService {
    FavoriteListResponse getFavorites(int page, int size);
    ProductResponse getProductById(Long id);
    ProductResponse addToFavorite(Long userId, Long productId);
    void deleteFromFavorite(Long userId, Long productId);

    void addFavoriteEntity(User user, Product product);
}