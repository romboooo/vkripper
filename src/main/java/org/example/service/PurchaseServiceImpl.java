package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;
import org.example.entity.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseServiceImpl implements PurchaseService {
    private final ShoppingCartServiceImpl shoppingCartService;
    private final UserServiceImpl userService;

    @Override
    @Secured("BUYER")
    public PurchaseResponse createPurchase(Long buyerId, PurchaseRequest request) {
        User buyer = userService.getUserEntityById(buyerId);
        ShoppingCart cartItem = shoppingCartService.getCartEntityById(request.getCartItemId());
        User seller = cartItem.getProduct().getSeller();

        if (cartItem.getUser().getId() != buyer.getId()) {
            throw new RuntimeException("Элемент корзины не принадлежит пользователю");
        }

        Product product = cartItem.getProduct();
        BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf((long) request.getAmount()));

        return switch (request.getPurchaseType()) {
            case SELLER -> handleSellerPurchase(buyer, product, totalCost);
            case OZON -> handleOzonPurchase(buyer, product);
            case BALANCE -> handleBalancePurchase(buyer, seller, totalCost, request.getAmount(), cartItem);
        };
    }

    private PurchaseResponse handleSellerPurchase(User buyer, Product product, BigDecimal totalCost) {
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

    private PurchaseResponse handleBalancePurchase(User buyer, User seller, BigDecimal totalCost, int amount, ShoppingCart cartItem) {
        if (buyer.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Недостаточно средств. Требуется: " + totalCost +
                    ", Доступно: " + buyer.getBalance());
        }
        buyer.setBalance(buyer.getBalance().subtract(totalCost));
        seller.setBalance(seller.getBalance().add(totalCost));
        userService.saveUser(buyer);
        userService.saveUser(seller);
        shoppingCartService.updateProductAmount(buyer.getId(), cartItem.getId(), cartItem.getAmount()-amount);
        return new PurchaseResponse(
                System.currentTimeMillis(),
                "COMPLETED",
                "Покупка успешно оформлена",
                null
        );
    }
}