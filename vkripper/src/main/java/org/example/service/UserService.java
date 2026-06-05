package org.example.service;

import org.example.dto.response.FinancialOperationResponse;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.springframework.security.access.annotation.Secured;

import java.math.BigDecimal;

public interface UserService {
    FinancialOperationResponse addMoney(Long id, BigDecimal amount);

    FinancialOperationResponse witdrawMoney(Long id, BigDecimal amount);

    User getUserEntityById(Long id);

    void saveUser(User user);

    UserResponse banUser(Long userId);

    UserResponse unbanUser(Long userId);
}
