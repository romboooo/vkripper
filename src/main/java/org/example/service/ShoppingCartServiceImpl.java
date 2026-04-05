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
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService{

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductService productService;
    private final UserServiceImpl userService;
    private final FavoriteService favoriteService;

    @Override
    public ShoppingCartListResponse getByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShoppingCart> cartPage = shoppingCartRepository.findByUserId(userId, pageable);
        return ShoppingCartListResponse.fromPage(cartPage);
    }

    @Override
    public ProductResponse getProductById(Long productId, Long userId) {
        ShoppingCart cartItem = shoppingCartRepository.findFirstByUserIdAndProductId(userId, productId);
        if (cartItem == null) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        return ProductResponse.fromProduct(cartItem.getProduct());
    }

    @Override
    public ShoppingCartResponse addToShoppingCart(Long userId, Long productId) {
        User user = userService.getUserEntityById(userId);
        Product product = productService.getProductEntityById(productId);

        if (!product.isAvailable()) {
            throw new RuntimeException("Товар недоступен");
        }

        boolean exists = shoppingCartRepository.existsByUserIdAndProductId(userId, productId);
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
    public void deleteFromShoppingCart(Long userId, Long productId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        shoppingCartRepository.deleteAll(cartItems);
    }

    @Override
    public void removeAllFromShoppingCart(Long userId) {
        List<ShoppingCart> items = shoppingCartRepository.findAllByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }
        shoppingCartRepository.deleteAll(items);
    }

    @Override
    public void addToFavoriteFromCart(Long userId, Long productId) {
        // Находим элемент корзины
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }

        ShoppingCart cartItem = cartItems.get(0);

        // Полностью делегируем логику добавления в избранное сервису FavoriteService
        // Внутри addFavoriteEntity уже есть проверка на дубликат и сохранение
        favoriteService.addFavoriteEntity(cartItem.getUser(), cartItem.getProduct());
    }

    @Override
    public ShoppingCartResponse updateProductAmount(Long userId, Long productId, int newAmount) {
        if (newAmount < 1) {
            throw new RuntimeException("Количество должно быть больше 0");
        }

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
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
