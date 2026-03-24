package org.example.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.entity.Favorite;
import org.example.entity.Product;
import org.example.entity.ShoppingCart;
import org.example.entity.User;
import org.example.repository.FavoriteRepository;
import org.example.repository.ProductRepository;
import org.example.repository.ShoppingCartRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;

    public ShoppingCartListResponse getByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShoppingCart> cartPage = shoppingCartRepository.findByUserId(userId, pageable);
        return ShoppingCartListResponse.fromPage(cartPage);
    }

    public ProductResponse getProductById(Long id){
        ShoppingCart shoppingCart = shoppingCartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with id" + id + "not found"));
        Product product = shoppingCart.getProduct();

        if (!product.isAvailable()){
            throw new RuntimeException("Product is not available");
        }
        return ProductResponse.fromProduct(product);
    }

    public ShoppingCartResponse addToShoppingCart(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id " + userId +"is not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product with id " + productId+" is not found"));

        if (!product.isAvailable()){
            throw new RuntimeException("Product with id " + productId+ " is not available");
        }

        boolean exists = shoppingCartRepository.existsByUserIdAndProductId(userId, productId);
        if (exists) {
            throw new RuntimeException("Product with id " + productId + "already in shopping cart");
        }

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(1);

        shoppingCartRepository.save(cartItem);

        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    public void deleteFromShoppingCart(Long userId, Long productId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException(
                    "Cart item with productId " + productId + " for userId " + userId + " not found"
            );
        }
        shoppingCartRepository.deleteAll(cartItems);
    }

    public void removeAllFromShoppingCart(Long userId) {
        List<ShoppingCart> items = shoppingCartRepository.findAllByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Shopping cart is already empty for userId " + userId);
        }
        shoppingCartRepository.deleteAll(items);
    }


    public void addToFavoriteFromCart(Long userId, Long productId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException(
                    "Product with id " + productId + " not found in shopping cart of user " + userId
            );
        }
        List<Favorite> existingFavorites = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (!existingFavorites.isEmpty()) {
            throw new RuntimeException("Product with id " + productId + " is already in favorites");
        }
        ShoppingCart cartItem = cartItems.get(0);
        Favorite favorite = new Favorite();
        favorite.setUser(cartItem.getUser());
        favorite.setProduct(cartItem.getProduct());

        favoriteRepository.save(favorite);
    }

    public ShoppingCartResponse updateProductAmount(Long userId, Long productId, int newAmount) {
        if (newAmount < 1) {
            throw new RuntimeException("Amount must be at least 1");
        }

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException(
                    "Cart item with productId " + productId + " for userId " + userId + " not found"
            );
        }

        ShoppingCart cartItem = cartItems.get(0);
        cartItem.setAmount(newAmount);
        shoppingCartRepository.save(cartItem);

        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }
}
