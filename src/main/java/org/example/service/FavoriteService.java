package org.example.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.FavoriteResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Favorite;
import org.example.entity.Product;
import org.example.repository.FavoriteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteListResponse getCatalog(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt"));
        Page<Favorite> favorites = favoriteRepository.findAll(pageable);
        return FavoriteListResponse.fromPage(favorites);
    }

    public ProductResponse getById(Long id){
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        Product product = favorite.getProduct();
        if (!product.isAvailable()){
            throw new RuntimeException("Product is not available");
        }
        return ProductResponse.fromProduct(product);
    }

}
