package org.example.service;

import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;

public interface FavoriteService {

    FavoriteListResponse getCatalog(int page, int size);

    ProductResponse getProductById(Long id);

    ProductResponse addToFavorite(Long userId, Long productId);

    void deleteFromFavorite(Long userId, Long productId);
}
