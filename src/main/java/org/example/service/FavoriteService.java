package org.example.service;

import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.User;

public interface FavoriteService {
    FavoriteListResponse getCatalog(int page, int size);
    ProductResponse getProductByUsername(String username);
    ProductResponse getProductById(Long id);
    ProductResponse addToFavorite(String username, Long productId);
    void deleteFromFavorite(String username, Long productId);

    void addFavoriteEntity(User user, Product product);
}