package org.example.service;


import lombok.RequiredArgsConstructor;
import org.example.dto.response.ProductResponse;
import org.example.dto.response.ShoppingCartListResponse;
import org.example.dto.response.ShoppingCartResponse;
import org.example.entity.Product;
import org.example.entity.ShoppingCart;
import org.example.entity.ShoppingCartUpdate;
import org.example.entity.User;
import org.example.repository.ShoppingCartRepository;
import org.example.repository.ShoppingCartUpdateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService{

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartUpdateRepository shoppingCartUpdateRepository;
    private final ProductService productService;
    private final UserServiceImpl userService;
    private final FavoriteService favoriteService;

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    public ShoppingCartListResponse getByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShoppingCart> cartPage = shoppingCartRepository.findByUserId(userId, pageable);
        return ShoppingCartListResponse.fromPage(cartPage);
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    public ProductResponse getProductById(Long productId, Long userId) {
        ShoppingCart cartItem = shoppingCartRepository.findFirstByUserIdAndProductId(userId, productId);
        if (cartItem == null) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        return ProductResponse.fromProduct(cartItem.getProduct());
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    @Transactional
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
        cartItem.setAmountInCart(1);
        shoppingCartRepository.save(cartItem);
        updateShoppingCart(user);
        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    public void validateCartAddForProcess(Long userId, Long productId) {
        Product product = productService.getProductEntityById(productId);
        userService.getUserEntityById(userId);

        if (!product.isAvailable()) {
            throw new RuntimeException("Товар недоступен");
        }
        if (shoppingCartRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Товар уже в корзине");
        }
    }

    @Transactional
    public ShoppingCartResponse addToShoppingCartForProcess(Long userId, Long productId) {
        validateCartAddForProcess(userId, productId);

        User user = userService.getUserEntityById(userId);
        Product product = productService.getProductEntityById(productId);
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setAmountInCart(1);
        shoppingCartRepository.save(cartItem);
        updateShoppingCart(user);
        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    @Transactional
    public void deleteFromShoppingCart(Long userId, Long productId) {
        User user = userService.getUserEntityById(userId);
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        shoppingCartRepository.deleteAll(cartItems);
        updateShoppingCart(user);
    }

    public void validateCartRemoveForProcess(Long userId, Long productId) {
        userService.getUserEntityById(userId);
        if (shoppingCartRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }
    }

    @Transactional
    public void deleteFromShoppingCartForProcess(Long userId, Long productId) {
        validateCartRemoveForProcess(userId, productId);
        User user = userService.getUserEntityById(userId);
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        shoppingCartRepository.deleteAll(cartItems);
        updateShoppingCart(user);
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    @Transactional
    public void removeAllFromShoppingCart(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<ShoppingCart> items = shoppingCartRepository.findAllByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }
        shoppingCartRepository.deleteAll(items);
        updateShoppingCart(user);
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    @Transactional
    public void addToFavoriteFromCart(Long userId, Long productId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }
        ShoppingCart cartItem = cartItems.get(0);
        favoriteService.addFavoriteEntity(cartItem.getUser(), cartItem.getProduct());
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    @Transactional
    public ShoppingCartResponse updateProductAmount(Long userId, Long productId, int newAmount) {
        if (newAmount < 1) {
            throw new RuntimeException("Количество должно быть больше 0");
        }

        User user = userService.getUserEntityById(userId);

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }

        ShoppingCart cartItem = cartItems.get(0);
        cartItem.setAmountInCart(newAmount);
        shoppingCartRepository.save(cartItem);
        updateShoppingCart(user);
        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    public void validateCartUpdateForProcess(Long userId, Long productId, int newAmount) {
        if (newAmount < 1) {
            throw new RuntimeException("Количество должно быть больше 0");
        }
        userService.getUserEntityById(userId);
        if (shoppingCartRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            throw new RuntimeException("Товар не найден в корзине");
        }
    }

    @Transactional
    public ShoppingCartResponse updateProductAmountForProcess(Long userId, Long productId, int newAmount) {
        validateCartUpdateForProcess(userId, productId, newAmount);
        User user = userService.getUserEntityById(userId);
        ShoppingCart cartItem = shoppingCartRepository.findByUserIdAndProductId(userId, productId).get(0);
        cartItem.setAmountInCart(newAmount);
        shoppingCartRepository.save(cartItem);
        updateShoppingCart(user);
        return ShoppingCartResponse.fromShoppingCart(cartItem);
    }

    @Override
    public ShoppingCart getCartEntityById(Long id) {
        return shoppingCartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Элемент корзины не найден"));
    }

    @Override
    @Transactional
    public void deleteCartEntity(ShoppingCart cartItem) {
        shoppingCartRepository.delete(cartItem);
    }



    private void updateShoppingCart(User user) {
        ShoppingCartUpdate update = shoppingCartUpdateRepository.findFirstByUser(user);

        if (update == null) {
            update = createShoppingCartUpdate(user);
        }

        update.setLastUpdatedAt(LocalDateTime.now());
        shoppingCartUpdateRepository.save(update);
    }

    private ShoppingCartUpdate createShoppingCartUpdate(User user) {
        ShoppingCartUpdate update = new ShoppingCartUpdate();
        update.setUser(user);
        return update;
    }

    @Override
    @Transactional
    public int deleteOldShoppingCarts(LocalDateTime threshold) {
        List<ShoppingCartUpdate> oldUpdates = shoppingCartUpdateRepository.findByLastUpdatedAtBefore(threshold);

        for (ShoppingCartUpdate update : oldUpdates) {
            User user = update.getUser();

            shoppingCartRepository.deleteAllByUser(user);
            shoppingCartUpdateRepository.deleteAllByUser(user);
        }

        return oldUpdates.size();
    }

}
