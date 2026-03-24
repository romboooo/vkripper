package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Favorite;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.FavoriteRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public FavoriteListResponse getCatalog(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt"));
        Page<Favorite> favorites = favoriteRepository.findAll(pageable);
        return FavoriteListResponse.fromPage(favorites);
    }

    public ProductResponse getProductById(Long id){
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        Product product = favorite.getProduct();
        if (!product.isAvailable()){
            throw new RuntimeException("Product is not available");
        }
        return ProductResponse.fromProduct(product);
    }

    public ProductResponse addToFavorite(Long userId, Long productId){
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product with id {id} not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new RuntimeException("User with id {id} not found"));

            if (!product.isAvailable()) {
                throw new RuntimeException("Product with id {id} is not available");
            }
            boolean exists = favoriteRepository.findByUserAndProduct(user, product).isPresent();
            if (exists) {
                throw new RuntimeException("Product with id {id} already in favorites");
            }

            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProduct(product);

            Favorite savedFavorite = favoriteRepository.save(favorite);
            return ProductResponse.fromProduct(savedFavorite.getProduct());

    }

    public void deleteFromFavorite(Long productId){
        Favorite favorite = favoriteRepository.findById(productId)
                .orElseThrow(() ->new RuntimeException("Favorite product with id {id} not found"));
        favoriteRepository.delete(favorite);
    }

}
