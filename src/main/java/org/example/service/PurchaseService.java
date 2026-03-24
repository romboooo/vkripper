package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;
import org.example.entity.*;
import org.example.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    public PurchaseResponse createPurchase(PurchaseRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + request.getUserId() + " не найден"));

        ShoppingCart cartItem = shoppingCartRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new RuntimeException("Элемент корзины с id " + request.getCartItemId() + " не найден"));

        if (cartItem.getUser().getId() != user.getId()) {
            throw new RuntimeException("Элемент корзины не принадлежит пользователю");
        }

        Product product = cartItem.getProduct();
        int amount = cartItem.getAmount();
        BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(amount));

        return switch (request.getPurchaseType()) {
            case SELLER -> handleSellerPurchase(user, product, amount);
            case OZON -> handleOzonPurchase(user, product);
            case BALANCE -> handleBalancePurchase(user, product, amount, totalCost, cartItem);
        };
    }

    private PurchaseResponse handleSellerPurchase(User user, Product product, int amount) {
        Long sellerId = product.getSeller().getId();
        return new PurchaseResponse(
                null,
                "PENDING",
                "Свяжитесь с продавцом (ID: " + sellerId + ") для оформления заказа",
                null
        );
    }

    private PurchaseResponse handleOzonPurchase(User user, Product product) {
        return new PurchaseResponse(
                null,
                "REDIRECT",
                "Вы перешли в Ozon",
                "https://ozon.ru/t/yCwkBpx"
        );
    }

    private PurchaseResponse handleBalancePurchase(User user, Product product, int amount, BigDecimal totalCost, ShoppingCart cartItem) {
        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Недостаточно средств. Требуется: " + totalCost +
                    ", Доступно: " + user.getBalance());
        }

        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        shoppingCartRepository.delete(cartItem);

        return new PurchaseResponse(
                System.currentTimeMillis(),
                "COMPLETED",
                "Покупка успешно оформлена",
                null
        );
    }
}

