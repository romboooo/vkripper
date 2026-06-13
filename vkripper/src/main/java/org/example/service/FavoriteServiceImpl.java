package org.example.service;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductServiceImpl productService;
    private final UserServiceImpl userService;

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    public FavoriteListResponse getFavorites(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt"));
        Page<Favorite> favorites = favoriteRepository.findAll(pageable);
        return FavoriteListResponse.fromPage(favorites);
    }
    @Override
    @PreAuthorize("hasAuthority('BUYER')")
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
    @PreAuthorize("hasAuthority('BUYER')")
    @Transactional
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

    public void validateFavoriteAddForProcess(Long userId, Long productId) {
        Product product = productService.getProductEntityById(productId);
        User user = userService.getUserEntityById(userId);

        if (!product.isAvailable()) {
            throw new RuntimeException("Product with id " + productId + " is not available");
        }
        if (!favoriteRepository.findByUserAndProduct(user, product).isEmpty()) {
            throw new RuntimeException("Product with id " + productId + " is already in favorites");
        }
    }

    @Transactional
    public ProductResponse addToFavoriteForProcess(Long userId, Long productId) {
        validateFavoriteAddForProcess(userId, productId);
        Product product = productService.getProductEntityById(productId);
        User user = userService.getUserEntityById(userId);

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);

        Favorite savedFavorite = favoriteRepository.save(favorite);
        return ProductResponse.fromProduct(savedFavorite.getProduct());
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    @Transactional
    public void deleteFromFavorite(Long userId, Long productId){
        List<Favorite> favorites = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (favorites.isEmpty()) {
            throw new RuntimeException(
                    "Favorite product with id " + productId + " for userId " + userId + " not found"
            );
        }
        favoriteRepository.deleteAll(favorites);
    }

    public void validateFavoriteRemoveForProcess(Long userId, Long productId) {
        userService.getUserEntityById(userId);
        if (favoriteRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            throw new RuntimeException(
                    "Favorite product with id " + productId + " for userId " + userId + " not found"
            );
        }
    }

    @Transactional
    public void deleteFromFavoriteForProcess(Long userId, Long productId) {
        validateFavoriteRemoveForProcess(userId, productId);
        favoriteRepository.deleteAll(favoriteRepository.findByUserIdAndProductId(userId, productId));
    }

    @Override
    @Transactional
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
    @Transactional
    public void save(Favorite favorite){
        favoriteRepository.save(favorite);
    }
}
