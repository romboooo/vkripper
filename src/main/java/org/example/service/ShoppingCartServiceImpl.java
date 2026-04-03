package org.example.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.entity.Product;
import org.example.entity.ShoppingCart;
import org.example.entity.User;
import org.example.repository.ShoppingCartRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService{

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductService productService;
    private final UserService userService;
    private final FavoriteService favoriteService;

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public ShoppingCartListResponse getByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShoppingCart> cartPage = shoppingCartRepository.findByUserUsername(username, pageable);
        return ShoppingCartListResponse.fromPage(cartPage);
    }

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public ProductResponse getProductById(Long productId, String username) {
        ShoppingCart cartItem = shoppingCartRepository.findFirstByUserUsernameAndProductId(username, productId);
        if (cartItem == null) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        return ProductResponse.fromProduct(cartItem.getProduct());
    }

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public ShoppingCartResponse addToShoppingCart(String username, Long productId) {
        User user = userService.getUserEntityByUsername(username);
        Product product = productService.getProductEntityById(productId);

        if (!product.isAvailable()) {
            throw new RuntimeException("Товар недоступен");
        }

        boolean exists = shoppingCartRepository.existsByUserUsernameAndProductId(username, productId);
        if (exists) {
            throw new RuntimeException("Товар уже в корзине");
        }

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmount(1);
        shoppingCartRepository.save(cartItem);
        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public void deleteFromShoppingCart(String username, Long productId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserUsernameAndProductId(username, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        shoppingCartRepository.deleteAll(cartItems);
    }

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public void removeAllFromShoppingCart(String username) {
        List<ShoppingCart> items = shoppingCartRepository.findAllByUserUsername(username);
        if (items.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }
        shoppingCartRepository.deleteAll(items);
    }

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public void addToFavoriteFromCart(String username, Long productId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserUsernameAndProductId(username, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }

        ShoppingCart cartItem = cartItems.get(0);
        favoriteService.addFavoriteEntity(cartItem.getUser(), cartItem.getProduct());
    }

    @Override
    @PreAuthorize("hasRole('BUYER')")
    public ShoppingCartResponse updateProductAmount(String username, Long productId, int newAmount) {
        if (newAmount < 1) {
            throw new RuntimeException("Количество должно быть больше 0");
        }

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserUsernameAndProductId(username, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }

        ShoppingCart cartItem = cartItems.get(0);
        cartItem.setAmount(newAmount);
        shoppingCartRepository.save(cartItem);
        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    @Override
    public ShoppingCart getCartEntityById(Long id) {
        return shoppingCartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Элемент корзины не найден"));
    }

    @Override
    public void deleteCartEntity(ShoppingCart cartItem) {
        shoppingCartRepository.delete(cartItem);
    }
}
