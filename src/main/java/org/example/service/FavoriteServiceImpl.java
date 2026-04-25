package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.FavoriteListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Favorite;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.FavoriteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductServiceImpl productService;
    private final UserServiceImpl userService;

    @Override
    @Secured("BUYER")
    public FavoriteListResponse getCatalog(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt"));
        Page<Favorite> favorites = favoriteRepository.findAll(pageable);
        return FavoriteListResponse.fromPage(favorites);
    }
    @Override
    @Secured("BUYER")
    public ProductResponse getProductById(Long id){
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        Product product = favorite.getProduct();
        if (!product.isAvailable()){
            throw new RuntimeException("Product is not available");
        }
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Secured("BUYER")
    public ProductResponse addToFavorite(Long userId, Long productId){
        Product product = productService.getProductEntityById(productId);
        User user = userService.getUserEntityById(userId);

        if (!product.isAvailable()) {
            throw new RuntimeException("Product with id " + productId + " is not available");
        }
        List<Favorite> existingFavorites = favoriteRepository.findByUserAndProduct(user, product);
        if (!existingFavorites.isEmpty()) {
            throw new RuntimeException("Product with id " + productId + " is already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);

        Favorite savedFavorite = favoriteRepository.save(favorite);
        return ProductResponse.fromProduct(savedFavorite.getProduct());
    }

    @Override
    @Secured({"BUYER", "MODERATOR", "ADMIN"})
    public void deleteFromFavorite(Long userId, Long productId){
        List<Favorite> favorites = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (favorites.isEmpty()) {
            throw new RuntimeException(
                    "Favorite product with id " + productId + " for userId " + userId + " not found"
            );
        }
        favoriteRepository.deleteAll(favorites);
    }

    @Override
    public void addFavoriteEntity(User user, Product product) {
        List<Favorite> existingFavorites = favoriteRepository.findByUserAndProduct(user, product);
        if (!existingFavorites.isEmpty()) {
            throw new RuntimeException("Товар уже есть в избранном");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
    }

    public void save(Favorite favorite){
        favoriteRepository.save(favorite);
    }
}
