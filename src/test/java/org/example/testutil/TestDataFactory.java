package org.example.testutil;

import org.example.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TestDataFactory {

    public static User createUser(String username, BigDecimal balance) {
        User user = new User();
        user.setUsername(username);
        user.setBalance(balance);
        user.setFavorites(List.of());
        return user;
    }

    public static Product createProduct(String name, BigDecimal price, ProductGroup group, User seller) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setAvailable(true);
        product.setProductGroup(group);
        product.setSeller(seller);
        return product;
    }

    public static Review createReview(User user, Product product, String text, Integer rating) {
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setText(text);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    public static Favorite createFavorite(User user, Product product) {
        Favorite fav = new Favorite();
        fav.setUser(user);
        fav.setProduct(product);
        fav.setAddedAt(LocalDateTime.now());
        return fav;
    }

    public static ShoppingCart createCartItem(User user, Product product, int amount) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setAmountInCart(amount);
        return cart;
    }
}