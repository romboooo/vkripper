package org.example.service;

import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.springframework.security.access.annotation.Secured;

import java.math.BigDecimal;

public interface UserService {
    UserResponse addMoney(Long id, BigDecimal amount);

    @Secured("BUYER")
    UserResponse witdrawMoney(Long id, BigDecimal amount);

    User getUserEntityById(Long id);

    void saveUser(User user);
}
