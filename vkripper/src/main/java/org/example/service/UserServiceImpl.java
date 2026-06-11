package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.entity.FinancialOperation;
import org.example.common.enums.FinancialOperationStatus;
import org.example.common.enums.FinancialOperationType;
import org.example.common.repository.FinancialOperationRepository;
import org.example.dto.response.FinancialOperationResponse;
import org.example.dto.response.UserResponse;
import org.example.entity.Product;
import org.example.entity.Review;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.messaging.FinancialOperationEventPublisher;
import org.example.repository.ProductRepository;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final FinancialOperationRepository financialOperationRepository;
    private final FinancialOperationEventPublisher financialOperationEventPublisher;
    private final TransactionTemplate transactionTemplate;

    @Override
    @PreAuthorize("hasAuthority('BUYER')")
    public FinancialOperationResponse addMoney(Long id, BigDecimal amount){
        getUserEntityById(id);
        return createMoneyOperation(id, FinancialOperationType.TOP_UP, amount,
                "Пополнение баланса принято в обработку");
    }

    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER')")
    @Override
    public FinancialOperationResponse witdrawMoney(Long id, BigDecimal amount){
        User user = getUserEntityById(id);
        if(user.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("недостаточно средств на счете");
        }
        return createMoneyOperation(id, FinancialOperationType.WITHDRAW, amount,
                "Вывод средств принят в обработку");
    }

    private FinancialOperationResponse createMoneyOperation(
            Long userId,
            FinancialOperationType type,
            BigDecimal amount,
            String message
    ) {
        return transactionTemplate.execute(status -> {
            FinancialOperation operation = new FinancialOperation();
            operation.setType(type);
            operation.setUserId(userId);
            operation.setAmount(amount);
            operation.setCurrency("RUB");
            operation.setStatus(FinancialOperationStatus.PENDING);
            operation = financialOperationRepository.save(operation);

            financialOperationEventPublisher.publishAfterCommit(operation);
            return FinancialOperationResponse.fromOperation(operation, message);
        });
    }

    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
    }
    @Override
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse banUser(Long userId) {
        User user = getUserEntityById(userId);

        if (user.isBanned()) {
            throw new IllegalStateException("User is already banned");
        }

        return transactionTemplate.execute(status -> {
            User persistentUser = getUserEntityById(userId);
            persistentUser.setBanned(true);
            userRepository.save(persistentUser);
//            if (true) throw new RuntimeException("Test rollback in banUser method");
            if (persistentUser.getRole() == Role.SELLER) {
                List<Product> userProducts = productRepository.findBySellerId(userId);
                for (Product product : userProducts) {
                    product.setHidden(true);
                    product.setAvailable(false);
                }
                productRepository.saveAll(userProducts);
            }

            List<Review> userReviews = reviewRepository.findByUserId(userId);
            for (Review review : userReviews) {
                review.setHidden(true);
            }
            reviewRepository.saveAll(userReviews);
            return UserResponse.fromUser(persistentUser);
                });
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse unbanUser(Long userId) {
        User user = getUserEntityById(userId);

        if (!user.isBanned()) {
            throw new IllegalStateException("User is not banned");
        }

        return transactionTemplate.execute(status -> {
            User persistentUser = getUserEntityById(userId);

            persistentUser.setBanned(false);
            userRepository.save(persistentUser);
//            if (true) throw new RuntimeException("Test rollback in unbanUser method");

            if (persistentUser.getRole() == Role.SELLER) {
                List<Product> userProducts = productRepository.findBySellerId(userId);
                for (Product product : userProducts) {
                    product.setHidden(false);
                    product.setAvailable(true);
                }
                productRepository.saveAll(userProducts);
            }

            List<Review> userReviews = reviewRepository.findByUserId(userId);
            for (Review review : userReviews) {
                review.setHidden(false);
            }
            reviewRepository.saveAll(userReviews);

            return UserResponse.fromUser(persistentUser);
        });
    }
}
